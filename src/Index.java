import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;

public class Index {

    static int recordSize = 40;
    Hashtable<Integer, ArrayList<String>> hashIndex = new Hashtable<>();
    ArrayList<ArrayList<String>> arrayIndex = new ArrayList<>(5000);


    /**
     * Reads the specified file
     *
     * @param fileNumber the blockId that is meant to be read
     * @return the contents of the file
     */
    public static String readFile(int fileNumber) {
        String filename = "Project2Dataset/Project2Dataset/F" + fileNumber + ".txt";
        String blockContent = null;
        try {
            FileReader fileReader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            blockContent = bufferedReader.readLine();

            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return blockContent;
    }


    /**
     * Gets the specified record
     *
     * @param recordNumber
     * @return
     */
    public static String getRecord(int recordNumber, String fileContent) {
        int start = ((recordNumber - 1) % 100) * recordSize; //start location

        return fileContent.substring(start, start + 40);
    }


    /**
     * reads all the files in directory and each record in each file
     */
    public void readDirectory() {

        int numFilesInDirectory = fileCount("Project2Dataset/Project2Dataset/");

        for (int i = 0; i < 5000; i++) {
            this.arrayIndex.add(new ArrayList<>());
        }


        //for each file in the directory
        for (int i = 1; i <= numFilesInDirectory; i++) {
            String fileContent = readFile(i);
            //for each record
            for (int k = 1; k <= 100; k++) {
                String record = getRecord(k, fileContent);
                int recordLen = record.length();
                int randomV = Integer.parseInt(record.substring(recordLen - 7, recordLen - 3));
                int offset = ((k - 1) % 100) * recordSize; //start location
                String storage = "F" + i + "-" + offset; //record info

                if (!this.hashIndex.containsKey(randomV)) {
                    ArrayList<String> res = new ArrayList<>();
                    res.add(storage);
                    this.hashIndex.put(randomV, res);

                    //add the new info to the list
                    this.arrayIndex.get(randomV - 1).add(storage);

                } else {
                    //add the new location
                    this.hashIndex.get(randomV).add(storage);

                    //add the new info to the list
                    this.arrayIndex.get(randomV - 1).add(storage);
                }

            }
        }
    }


    /**
     * counts the number of files in dataset directory
     *
     * @param dirPath dataset directory
     * @return
     */
    public static int fileCount(String dirPath) {
        try {
            long fileCount = Files.list(Paths.get(dirPath)).count();
            return (int) fileCount;
        } catch (IOException e) {
            System.out.println("Error occurred: " + e.getMessage());
            return 0;
        }
    }
}
