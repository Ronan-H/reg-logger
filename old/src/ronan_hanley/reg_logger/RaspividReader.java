
package ronan_hanley.reg_logger;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class RaspividReader implements Runnable {
	private static final int blockSize = 4096;
	private static final int numBlocks = 16;
	
	private InputStream inputStream;
	private boolean running;
	
	private RaspicamListener camListener;
	
	private BufferedImage lastImage;
	
	public RaspividReader(int frameWidth, int frameHeight, RaspicamListener camListener) {
		this.camListener = camListener;
		
		String[] command = { "/bin/sh", "-c",
			String.format("raspivid -o - -t 0 -cd MJPEG -w %d -h %d -fps 25", frameWidth, frameHeight) };
		Runtime rt = Runtime.getRuntime();
		Process proc;
		try {
			proc = rt.exec(command);
			inputStream = proc.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		int i;
		int currBlock = 0;
		int blockPos = 0;
		int lastBlock = 0;
		int lastBlockPos = 0;
		byte[] block;
		int numImageBytes = 0;
		byte[] imageBytes;
		int offset;
		int imgBlock;
		int imgBlockPos;
		ByteArrayInputStream bais;
		BufferedImage nextImage;
		long imageTime = 0;
		long lastImageTime = System.nanoTime();
		boolean eofFound;
		int highestNumBytes = 0;
		int nextBlock;
		int byteCounter;
		int lastBlockRead = 0;
		int lastBlockPosRead = 0;
		int oldBytesFromStart;
		int newBytesFromStart;
		
		System.out.println("Running RaspividReader");
		
		running = true;
		
		DataInputStream in = new DataInputStream(inputStream);
		byte[][] blocks = new byte[numBlocks][blockSize];
		
		// initial first block read
		
		try {
			in.readFully(blocks[currBlock]);
			
			FileOutputStream fos = new FileOutputStream("./ImageByteDump/first_read.jpeg");
			fos.write(blocks[currBlock]);
		    fos.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		if (imageTime == 0) {
			imageTime = System.nanoTime();
		}
		
		while (running) {
			block = blocks[currBlock];
			
			for (blockPos = 0; blockPos < blockSize; ++blockPos) {
				//if (blockPos % 100 == 0) System.out.println("Byte: " + blocks[currBlock][blockPos]);
				
				if (blockPos == blockSize - 1) {
					// must read next block to see next byte
					nextBlock = (currBlock + 1) % numBlocks;
					try {
						in.readFully(blocks[nextBlock]);
						
						if (imageTime == 0) {
							imageTime = System.nanoTime();
						}
						/*
						FileOutputStream fos = new FileOutputStream(String.format("./ImageByteDump/block_%d.jpeg", nextBlock));
						fos.write(blocks[currBlock]);
					    fos.close();
					    */
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					eofFound = (block[blockPos] == (byte) 0xff
						&& blocks[nextBlock][0] == (byte) 0xd9);
					
					if (eofFound) {
						//System.out.println("EOF Found when reading ahead 1 block");
					}
				}
				else {
					eofFound = (block[blockPos] == (byte) 0xff && block[blockPos + 1] == (byte) 0xd9);
				}
				
				if (eofFound) {
					oldBytesFromStart = (lastBlock * blockSize) + lastBlockPos;
					newBytesFromStart = (currBlock * blockSize) + blockPos;
					
					//System.out.printf("Old:New bytes from start: %d:%d%n", oldBytesFromStart, newBytesFromStart);
					
					if (newBytesFromStart >= oldBytesFromStart) {
						numImageBytes = newBytesFromStart - oldBytesFromStart;
					}
					else {
						numImageBytes = (numBlocks * blockSize) - oldBytesFromStart + newBytesFromStart;
					}
					
					numImageBytes += 2;
					
					if (numImageBytes > highestNumBytes) {
						highestNumBytes = numImageBytes;
					}
					
					//System.out.println("Num image bytes: " + numImageBytes);
					//System.out.println("Highest image bytes: " + highestNumBytes);
					//System.out.printf("End of last iamge: block %d, byte %d%n", lastBlock, lastBlockPos);
					
					imageBytes = new byte[numImageBytes];
					
					byteCounter = 0;
					//System.out.println("Start of byte copy");
					while (byteCounter < numImageBytes) {
						offset = lastBlockPos + byteCounter;
						imgBlock = (lastBlock + (offset / blockSize)) % numBlocks;
						imgBlockPos = offset % blockSize;
						
						imageBytes[byteCounter++] = blocks[imgBlock][imgBlockPos];
						//System.out.printf("byte copy: %d,%d%n", imgBlock, imgBlockPos);
						
						// debug
						lastBlockRead = imgBlock;
						lastBlockPosRead = imgBlockPos;
					}
					
					//System.out.printf("lastBlock/lastBlockPos: %d, %d%n", lastBlock, lastBlockPos);
					//System.out.printf("currBlock/blockpos: %d, %d%n", currBlock, blockPos);
					//System.out.printf("Last block/pos read: %d, %d%n", lastBlockRead, lastBlockPosRead);
					
					try {
						//System.out.printf("Byte before image starts: 0x%02X%n", blocks[lastBlock][lastBlockPos - 1]);
					}
					catch(Exception e) {
						//System.out.println("Couldn't print byte before image");
					}
					//System.out.printf("First 3 image bytes: 0x%02X 0x%02X 0x%02X%n", imageBytes[0], imageBytes[1], imageBytes[2]);
					//System.out.printf("Last 2 image bytes: 0x%02X 0x%02X%n",
						//imageBytes[imageBytes.length - 2], imageBytes[imageBytes.length - 1]);
					
					if (imageBytes[imageBytes.length - 2] != (byte)0xff
						|| imageBytes[imageBytes.length - 1] != (byte)0xd9) {
						//System.out.printf("File end corrupt. Last 2 image bytes: 0x%02X 0x%02X%n",
							//imageBytes[imageBytes.length - 2], imageBytes[imageBytes.length - 1]);
						
						//System.out.println("currBlock: " + currBlock);
					}
					
					bais = new ByteArrayInputStream(imageBytes);
					try {
						nextImage = ImageIO.read(bais);
						
						//System.out.println("Time to process image bytes: " + (System.nanoTime() - imageTime));
						
						this.lastImage = nextImage;
						camListener.nextImageRetrieved(nextImage, imageTime - lastImageTime);
						lastImageTime = imageTime;
						imageTime = 0;
					} catch (IOException e) {
						System.out.printf("lastBlock/lastBlockPos: %d, %d%n", lastBlock, lastBlockPos);
						System.out.printf("Last block/pos read: %d, %d%n", lastBlockRead, lastBlockPosRead);
						System.out.printf("Last 2 image bytes: 0x%02X 0x%02X%n",
							imageBytes[imageBytes.length - 2], imageBytes[imageBytes.length - 1]);
						
						e.printStackTrace();
						try {
							FileOutputStream fos = new FileOutputStream("./ImageByteDump/dump.jpeg");
							fos.write(imageBytes);
						    fos.close();
						} catch(Exception e2) {
							e2.printStackTrace();
						}
					}
					
					lastBlock = currBlock;
					lastBlockPos = blockPos + 2;
					
					if (lastBlockPos > blockSize - 1) {
						//System.out.println("Last block wrap around");
						lastBlock = (lastBlock + 1) % numBlocks;
						lastBlockPos %= blockSize;
					}
					
					//System.out.println();
				}
			}
			
			currBlock = (currBlock + 1) % numBlocks;
		}
	}
	
	
	public BufferedImage getLastImage() {
		return lastImage;
	}
	
}
