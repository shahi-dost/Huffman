import java.util.Comparator;

public class TreeComparator implements Comparator<BinaryTree<CodeTreeElement>> {

    @Override
    public int compare(BinaryTree<CodeTreeElement> o1, BinaryTree<CodeTreeElement> o2) {
        Long freq1 = o1.getData().getFrequency();
        Long freq2 = o2.getData().getFrequency();
        return freq1.compareTo(freq2);
    }
}
