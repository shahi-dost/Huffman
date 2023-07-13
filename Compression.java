import java.util.*;
import java.io.*;

/*
Author: Shahidullah Dost
 */

public class Compression implements Huffman {

    public static void main(String[] args) throws IOException {
        Huffman counter = new Compression();

        //make the map to store the chars and their freq
        Map<Character, Long> charFrequencies = counter.countFrequencies("inputs/USConstitution.txt");

        // if there is nothing in the file it stops and states its empty
        if (charFrequencies.isEmpty()) {
            BufferedWriter output = new BufferedWriter(new FileWriter("inputs/USConstitution_decompressed.txt"));
            output.write("File is Empty");
        }
        //else it runs the rest of the code
        else {
            //make singular tree & make the combined trees containing all chars and freqs
            BinaryTree<CodeTreeElement> HuffmanTree = counter.makeCodeTree(charFrequencies);

            //assigns them all their unique 1 0 code
            Map<Character, String> codes = counter.computeCodes(HuffmanTree);

            //compressing the bit actual heckin file into a compressed file
            counter.compressFile(codes, "inputs/USConstitution.txt", "inputs/USConstitution_compressed.txt");

            // decompress into the outout file
            counter.decompressFile("inputs/USConstitution_compressed.txt", "inputs/USConstitution_decompressed.txt", HuffmanTree);
        }
    }

    @Override
    public Map<Character, Long> countFrequencies(String pathName) throws IOException {
        //open  file and store it and create a map with its letter and freq
        BufferedReader input = new BufferedReader(new FileReader(pathName));

        HashMap<Character, Long> store = new HashMap<>();
        try {
            // if it can read it makes the read into a char and if it is in the tree it adds one
            // to freq otherwise it creates a new node with a freq of 1 and the char
            int reading = input.read();
            while (reading != -1) {
                char character = (char) reading;

                if (!(store.containsKey(character))) {
                    store.put(character, 1L);
                } else {
                    store.put(character, ((Long) store.get(character) + 1));
                }
                // increments the read
                reading = input.read();
            }
            // close and return
        } finally {
            input.close();
        }
        return store;
    }

    @Override
    public BinaryTree<CodeTreeElement> makeCodeTree(Map<Character, Long> frequencies) {
        TreeComparator comparing = new TreeComparator();
        PriorityQueue<BinaryTree<CodeTreeElement>> pq = new PriorityQueue<>(frequencies.size(), comparing);

        for (char c : frequencies.keySet()) {
            CodeTreeElement hold = new CodeTreeElement(frequencies.get(c), c);
            BinaryTree<CodeTreeElement> miniTree = new BinaryTree<>(hold);
            pq.add(miniTree);
        }
        while (pq.size() > 1) {
            if (pq.size() == 1){
                BinaryTree<CodeTreeElement> T0 = pq.remove();
                BinaryTree<CodeTreeElement> THashTag = new BinaryTree<>(new CodeTreeElement(0L, '#'));
                CodeTreeElement store = new CodeTreeElement(T0.getData().getFrequency(), '#');
                BinaryTree<CodeTreeElement> frequencySum = new BinaryTree<>(store, T0, THashTag);
                pq.add(frequencySum);
            }
            else{
                BinaryTree<CodeTreeElement> T1 = pq.remove();
                BinaryTree<CodeTreeElement> T2 = pq.remove();
                CodeTreeElement store = new CodeTreeElement((T1.getData().getFrequency() + (T2.getData().getFrequency())), '#');
                BinaryTree<CodeTreeElement> frequencySum = new BinaryTree<>(store, T1, T2);
                pq.add(frequencySum);
            }
        }
        BinaryTree<CodeTreeElement> huffman = pq.remove();
        return huffman;
    }

    @Override
    public Map<Character, String> computeCodes(BinaryTree<CodeTreeElement> codeTree) {

        // try block most of it except opening file

        //traverse through the tree you made in makecodetree
        //so you had the tree take tree and assign left child zeroes and 1s to the right childs
        // do this recursively
        // string so far alr null in beginning
        String strSoFar = "";
        Map<Character, String> codes = new HashMap<>();
        helper(codeTree, strSoFar, codes);
        return codes;
    }

    private void helper(BinaryTree<CodeTreeElement> codeTree, String strSoFar, Map<Character, String> codes) {
        // if it is at a leaf it adds in the char with the code and resets the code
        if (codeTree.isLeaf()) {
            codes.put(codeTree.data.getChar(), strSoFar);
            strSoFar = "";
        }
        //if it has a left and not at a leaf it goes left and adds 0 to the end
        if (codeTree.hasLeft()) {
            helper(codeTree.getLeft(), strSoFar + "0", codes);
        }
        //if it has a right and not at a leaf it goes right and adds 1 to the end
        if (codeTree.hasRight()) {
            helper(codeTree.getRight(), strSoFar + "1", codes);
        }
    }

    @Override
    public void compressFile(Map<Character, String> codeMap, String pathName, String compressedPathName) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(pathName));
        BufferedBitWriter output = new BufferedBitWriter(compressedPathName);
        int storedRead = input.read();
        while (storedRead != -1) {
            String hold = codeMap.get((char) storedRead);
            for (int c = 0; c < hold.length(); c++) {
                output.writeBit(hold.charAt(c) == '1');
            }
            storedRead = input.read();
        }
        input.close();
        output.close();
    }

    @Override
    public void decompressFile(String compressedPathName, String decompressedPathName, BinaryTree<CodeTreeElement> codeTree) throws IOException {
        BufferedBitReader input = new BufferedBitReader(compressedPathName);
        BufferedWriter output = new BufferedWriter(new FileWriter(decompressedPathName));
// keeps the original tree
        BinaryTree<CodeTreeElement> storingTheOG = codeTree;

        while (input.hasNext()) {
            // takes the next bit as a boolean
            boolean bit = input.readBit();
            // if the boolean is false it checks if its a leaf if it is then it writes into the output and resets codetree
            // if it has no leafs it sets the tree to the left node
            if (!bit) {
                if (codeTree.getLeft().isLeaf()) {
                    output.write(codeTree.getLeft().data.myChar);
                    codeTree = storingTheOG;
                            // if tree is one initally make one null initially
                } else if (codeTree.hasLeft()) {
                    codeTree = codeTree.getLeft();
                }
                // does the same on the right
            } else if (bit) {
                if (codeTree.getRight().isLeaf()) {
                    output.write(codeTree.getRight().data.myChar);
                    codeTree = storingTheOG;
                } else if (codeTree.hasRight()) {
                    codeTree = codeTree.getRight();
                }

            }
        }
        // closes both files
        input.close();
        output.close();
    }
}


