import java.io.*;
import java.util.Scanner;

public class LZ77T {
    public static final int DEFAULT_BUFF_SIZE = 1024;
    protected int mBufferSize;
    protected Reader mIn;
    protected PrintWriter mOut;
    protected StringBuffer mSearchBuffer;

    public LZ77T() {
        this(DEFAULT_BUFF_SIZE);
    }

    public LZ77T(int buffSize) {
        mBufferSize = buffSize;
        mSearchBuffer = new StringBuffer(mBufferSize);
    }

    private void trimSearchBuffer() {
        if (mSearchBuffer.length() > mBufferSize) {
            mSearchBuffer = mSearchBuffer.delete(0, mSearchBuffer.length() - mBufferSize);
        }
    }

    public void compress(String infile) throws IOException {
        mIn = new BufferedReader(new FileReader(infile));
        mOut = new PrintWriter(new BufferedWriter(new FileWriter(infile + ".zip"))); // Saglabā kā .zip

        int nextChar;
        String currentMatch = "";
        int matchIndex = 0, tempIndex = 0;

        while ((nextChar = mIn.read()) != -1) {
            tempIndex = mSearchBuffer.indexOf(currentMatch + (char) nextChar);
            if (tempIndex != -1) {
                currentMatch += (char) nextChar;
                matchIndex = tempIndex;
            } else {
                String codedString = "~" + matchIndex + "~" + currentMatch.length() + "~" + (char) nextChar;
                String concat = currentMatch + (char) nextChar;
                if (codedString.length() <= concat.length()) {
                    mOut.print(codedString);
                    mSearchBuffer.append(concat);
                    currentMatch = "";
                    matchIndex = 0;
                } else {
                    currentMatch = concat;
                    matchIndex = -1;
                    while (currentMatch.length() > 1 && matchIndex == -1) {
                        mOut.print(currentMatch.charAt(0));
                        mSearchBuffer.append(currentMatch.charAt(0));
                        currentMatch = currentMatch.substring(1);
                        matchIndex = mSearchBuffer.indexOf(currentMatch);
                    }
                }
                trimSearchBuffer();
            }
        }

        if (matchIndex != -1) {
            String codedString = "~" + matchIndex + "~" + currentMatch.length();
            if (codedString.length() <= currentMatch.length()) {
                mOut.print(codedString);
            } else {
                mOut.print(currentMatch);
            }
        }

        mIn.close();
        mOut.flush();
        mOut.close();
    }

    public void unCompress(String infile, String outfile) throws IOException {
        mIn = new BufferedReader(new FileReader(infile)); // Pilns faila nosaukums
        mOut = new PrintWriter(new BufferedWriter(new FileWriter(outfile)));

        StreamTokenizer st = new StreamTokenizer(mIn);

        st.ordinaryChar((int) ' ');
        st.ordinaryChar((int) '.');
        st.ordinaryChar((int) '-');
        st.ordinaryChar((int) '\n');
        st.wordChars((int) '\n', (int) '\n');
        st.wordChars((int) ' ', (int) '}');

        int offset, length;
        while (st.nextToken() != StreamTokenizer.TT_EOF) {
            switch (st.ttype) {
                case StreamTokenizer.TT_WORD:
                    mSearchBuffer.append(st.sval);
                    mOut.print(st.sval);
                    trimSearchBuffer();
                    break;
                case StreamTokenizer.TT_NUMBER:
                    offset = (int) st.nval;
                    st.nextToken();
                    if (st.ttype == StreamTokenizer.TT_WORD) {
                        mSearchBuffer.append(offset + st.sval);
                        mOut.print(offset + st.sval);
                        break;
                    }
                    st.nextToken();
                    length = (int) st.nval;
                    String output = mSearchBuffer.substring(offset, offset + length);
                    mOut.print(output);
                    mSearchBuffer.append(output);
                    trimSearchBuffer();
                    break;
            }
        }

        mIn.close();
        mOut.flush();
        mOut.close();
    }

    public void displayFileSize(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            System.out.println("File size: " + file.length() + " bytes");
        } else {
            System.out.println("File not found: " + filename);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        LZ77T lz = new LZ77T();

        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1 - Compress a file");
            System.out.println("2 - Decompress a file");
            System.out.println("3 - Display file size");
            System.out.println("4 - Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            try {
                switch (choice) {
                    case 1: {
                        System.out.print("Enter the file name to compress: ");
                        String infile = scanner.nextLine();
                        lz.compress(infile);
                        System.out.println("File compressed successfully! Saved as: " + infile + ".zip");
                    }
                    case 2: {
                        System.out.print("Enter the file name to decompress (with full extension): ");
                        String infile = scanner.nextLine();
                        System.out.print("Enter the output file name: ");
                        String outfile = scanner.nextLine();
                        lz.unCompress(infile, outfile);
                        System.out.println("File decompressed successfully! Saved as: " + outfile);
                    }
                    case 3: {
                        System.out.print("Enter the file name to check size: ");
                        String filename = scanner.nextLine();
                        lz.displayFileSize(filename);
                    }
                    case 4: {
                        System.out.println("Exiting program.");
                        scanner.close();
                        System.exit(0);
                    }
                    default: System.out.println("Invalid choice. Please try again.");
                }
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
