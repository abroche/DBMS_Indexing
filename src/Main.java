import java.util.ArrayList;

import java.util.Scanner;


public class Main {

    public static void main(String[] args) {
        Index index = new Index();
        while (true) {
            System.out.println("Program is ready and waiting for user command.\n");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            long time;
            ArrayList<String> record;
            String structure;
            int dataBlocks;
            String cleanedInput = input.replace(" ", "").toLowerCase();

            if (input.equalsIgnoreCase("CREATE INDEX ON Project2Dataset (RandomV)")) {
                index.readDirectory();
                System.out.println("The hash-based and array-based indexes are built successfully.");
            } else {

                if (input.contains("!=")) { // not equal

                    int equalsIdx = cleanedInput.indexOf("=");
                    int v = Integer.parseInt(cleanedInput.substring(equalsIdx + 1));

                    //start time
                    long startTime = System.currentTimeMillis();
                    //if no indexes built then perform a table scan
                    record = performTableScanNotEqual(v);
                    long endTime = System.currentTimeMillis();
                    time = endTime - startTime;

                    structure = "Table Scan";
                    dataBlocks = 99;
                } else if (input.contains(">") || input.contains("<")) { //greater than or less than

                    int greater = cleanedInput.indexOf(">");
                    int less = cleanedInput.indexOf("<");
                    int and = cleanedInput.lastIndexOf("a") - 4;

                    int v1 = Integer.parseInt(cleanedInput.substring(greater + 1, and));
                    int v2 = Integer.parseInt(cleanedInput.substring(less + 1));

                    long startTime = System.currentTimeMillis();
                    if (index.arrayIndex.isEmpty()) {  //perform table scan
                        record = performTableScanGreaterLess(v1, v2);
                        long endTime = System.currentTimeMillis();
                        time = endTime - startTime;

                        structure = "Table Scan";
                        dataBlocks = 99;
                    } else {// we have an array index
                        ArrayList<String> recordResults = new ArrayList<>();
                        ArrayList<Integer> filesSeen = new ArrayList<>();
                        for (int i = v1; i < (v2 - 1); i++) {
                            //get the record info for that randomv
                            ArrayList<String> records = index.arrayIndex.get(i);
                            for (String r : records) {
                                int file = r.indexOf("-");
                                int F = r.indexOf("F");
                                int fileNum = Integer.parseInt(r.substring(F + 1, file));
                                if (!filesSeen.contains(fileNum)) {
                                    filesSeen.add(fileNum);
                                    String fileContent = Index.readFile(fileNum);
                                    //add all records whose randomv is between v1 and v2
                                    for (int j = 0; j < (100 * 40); j += 40) {
                                        String result = fileContent.substring(j, j + 40);
                                        int randomV = Integer.parseInt(result.substring(40 - 7, 40 - 3));
                                        if (randomV > v1 && randomV < v2) {
                                            recordResults.add(result);
                                        }
                                    }
                                }
                            }
                        }
                        long endTime = System.currentTimeMillis();

                        time = endTime - startTime;

                        structure = "Array-Based Index";
                        record = recordResults;
                        dataBlocks = filesSeen.size();
                    }


                } else { //equal
                    int equalsIdx = cleanedInput.indexOf("=");
                    int v = Integer.parseInt(cleanedInput.substring(equalsIdx + 1));
                    //start time
                    long startTime = System.currentTimeMillis();
                    //if no indexes built then perform a table scan
                    if (index.hashIndex.isEmpty()) {
                        record = performTableScan(v);
                        long endTime = System.currentTimeMillis();
                        time = endTime - startTime;

                        structure = "Table Scan";
                        dataBlocks = 99;
                    } else {
                        //search for equality using hash-based index
                        //print out record matching query

                        //gets the record info
                        //read the file from the record info
                        // get the record
                        ArrayList<Integer> filesSeen = new ArrayList<>();
                        record = index.hashIndex.get(v);
                        ArrayList<String> recordResults = new ArrayList<>();
                        for (String r : record) {
                            int file = r.indexOf("-");
                            int F = r.indexOf("F");
                            int fileNum = Integer.parseInt(r.substring(F + 1, file));
                            if (!filesSeen.contains(fileNum)) {
                                filesSeen.add(fileNum);
                                String fileContent = Index.readFile(fileNum);
                                //add all records whose randomv = i
                                for (int j = 0; j < (100 * 40); j += 40) {
                                    String result = fileContent.substring(j, j + 40);
                                    int randomV = Integer.parseInt(result.substring(40 - 7, 40 - 3));
                                    if (randomV == v) {
                                        recordResults.add(result);
                                    }
                                }
                            }
                        }
                        long endTime = System.currentTimeMillis();

                        time = endTime - startTime;

                        structure = "Hash-Based Index";
                        record = recordResults;
                        dataBlocks = filesSeen.size();
                    }
                }
                System.out.println("Record(s) matching query: " + record);
                System.out.println("Used: " + structure);
                System.out.println("Time taken: " + time + "ms");
                System.out.println("Data blocks read: " + dataBlocks);
            }
        }
    }

    /**
     * performs table scan when finding equality
     *
     * @param v randomV
     * @return all matching records
     */
    public static ArrayList<String> performTableScan(int v) {
        ArrayList<String> result = new ArrayList<>();

        //for each file in the directory
        for (int i = 1; i <= 99; i++) {
            String fileContent = Index.readFile(i);
            //for each record
            for (int k = 1; k <= 100; k++) {
                String record = Index.getRecord(k, fileContent);
                int recordLen = record.length();
                //get randomv
                int randomV = Integer.parseInt(record.substring(recordLen - 7, recordLen - 3));
                if (randomV == v) {
                    result.add(record);
                }
            }
        }
        return result;
    }

    /**
     * performs table scan when finding inequality
     *
     * @param v randomV
     * @return all non-matching records
     */
    public static ArrayList<String> performTableScanNotEqual(int v) {
        ArrayList<String> result = new ArrayList<>();

        //for each file in the directory
        for (int i = 1; i <= 99; i++) {
            String fileContent = Index.readFile(i);
            //for each record
            for (int k = 1; k <= 100; k++) {
                String record = Index.getRecord(k, fileContent);
                int recordLen = record.length();
                //get randomv
                int randomV = Integer.parseInt(record.substring(recordLen - 7, recordLen - 3));
                if (randomV != v) {
                    result.add(record);
                }
            }
        }
        return result;
    }


    /**
     * table scan when using range-based query
     *
     * @param v1 value randomV is greater than
     * @param v2 value randomV is less than
     * @return all records in between v1 and v2 not inclusive
     */
    public static ArrayList<String> performTableScanGreaterLess(int v1, int v2) {
        ArrayList<String> result = new ArrayList<>();

        //for each file in the directory
        for (int i = 1; i <= 99; i++) {
            String fileContent = Index.readFile(i);
            //for each record
            for (int k = 1; k <= 100; k++) {
                String record = Index.getRecord(k, fileContent);
                int recordLen = record.length();
                //get randomv
                int randomV = Integer.parseInt(record.substring(recordLen - 7, recordLen - 3));
                if (randomV > v1 && randomV < v2) {
                    result.add(record);
                }
            }
        }
        return result;
    }
}
