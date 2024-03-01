/**
 * !!! Podlegać modyfikacji mogę jedynie elementy oznaczone to do. !!!
 */

import org.javatuples.Pair;
import java.io.*;
import java.nio.file.Files;
import java.text.Collator;
import java.util.*;

public class SearchEngine {

    public String[] readFiles(String directory, MorfologyTool mt) {

        File folder = new File("files");
        HashSet<String> set = new HashSet<>();
        for (final File file : folder.listFiles()) {
            System.out.println(file.toString());
            String[] split = null;
            try {
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                String line;
                String text = "";
                while ((line = br.readLine()) != null)
                    text += line + " ";
                br.close();
                split = text.split("[^a-zA-Z0-9ąćęłńóśżźĄĆŁŃÓĘŚŻŹ]+");
            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            for (String s : split)
                if (toString().trim().length() > 1)
                    set.add(mt.getConcept(s.toLowerCase())); //dodaje formy podstawowe do seta
        }
        return set.toArray(String[]::new);
    }
    /**
     * Czytanie pliku i jego rozbiór morfologiczny
     * @param file
     * @return
     */
    public String[] readFile(File file) {
        String[] splitArr = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            String text = "";
            while ((line = br.readLine()) != null) {
                String lc = line.toLowerCase();
                text += lc + " ";
            }
            br.close();
            splitArr = text.split("[^a-zA-Z0-9ąćęłńóśżźĄĆŁŃÓĘŚŻŹ]+");
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return splitArr;
    }
    /**
     * Czytanie profili i scalanie ich (merge). Metoda zwraca słownik główny
     *
     */
    public String[] readProfiles() {
        File dir = new File("profiles");
        String[][] result = new String[dir.listFiles().length][];
        for (int i = 0; i < dir.listFiles().length; i++) {
            result[i] = readProfile(dir.listFiles()[i].getName());
        }
        int size = result.length;
        while (result.length >= 2) {
            String[] tmp = merge(result[result.length - 1], result[result.length - 2]);
            result[result.length - 2] = tmp;
            size--;
            String[][] resizeArr = new String[size][];
            for (int i = 0; i < resizeArr.length; i++) {
                resizeArr[i] = result[i];
            }
            result = resizeArr;
        }
        return result[0];
    }

    private String[] merge(String[] t1, String[] t2) {
        String[] merged = new String[t1.length + t2.length];
        for (int i = 0; i < merged.length; i++) {
            if (i <= t1.length-1)
                merged[i] = t1[i];
            else
                merged[i] = t2[i- t1.length];
        }
        return merged;
    }

    public String[] readProfile(String profileName) {
        String words = "";
        File file = new File(profileName);
        try {
            BufferedReader br = new BufferedReader(new FileReader("profiles/"+file));
            String line;
            while ((line = br.readLine()) != null){
                words += line + "\t";
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return words.split("\t");
    }

    /**
     * Tworzy plik indeksowy dla danego pliku tekstowego: w każdym wierszu jest pojęcie oraz jego liczba wystąpień w pliku
     * @param fileEntry
     * @param wordsL
     */
    int indexSize = 0;
    int revIndexSize = 0;
    public void makeIndex(File fileEntry, Pair<String, Integer>[] wordsL) {
        String fileName = fileEntry.getName();
        try {
            for (int i = 0; i < wordsL.length; i++) {
                if (wordsL[i].getValue1() > 0) {
                    indexSize+=1;
                    BufferedWriter bw = new BufferedWriter(new FileWriter("indices/" + wordsL[i].getValue0() + ".txt", true));
                    bw.write(fileName + "\t" + wordsL[i].getValue1() + "\n");
                    bw.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //tworzenie reversed indexow
        File reversedIndices = new File("reversedIndices");
        if (!reversedIndices.exists())
            reversedIndices.mkdir();
        try {
            for (int i = 0; i < wordsL.length; i++) {
                if (wordsL[i].getValue1() > 0) {
                    revIndexSize+=1;
                    BufferedWriter bw = new BufferedWriter(new FileWriter("reversedIndices/" + fileEntry.getName(), true));
                    bw.write(wordsL[i].getValue0() + "\t" + wordsL[i].getValue1() + "\n");
                    bw.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateIndex(File fileEntry, String word) {
    }

    /**
     * Zwraca pliki zawierające podane słowo
     * @param word wyszukiwane słowo
     * @return
     */
    public String[] getDocsContainingWord(String word) {
        String[] tmp = null;
        int index = 0;
        try {
            File f = new File("indices/" + word);
            tmp = new String[new File("files").listFiles().length];
            BufferedReader br = new BufferedReader(new FileReader(f+".txt"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitPair = line.split("\t");
                tmp[index] = splitPair[0];
                index++;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return trim(tmp);
    }

    public String[] getDocsContainingWords(String[] words) {
        MDictionary dict = new MDictionary();
        for (String word : words) {
            for (String file : getDocsContainingWord(word)) {
                dict.Add(file);
                dict.Find(file);
            }
        }

        String[] result = new String[dict.size];
        for (int i = 0; i < dict.getAppearedWordsWithCount().length; i++) {
            if (dict.getAppearedWordsWithCount()[i].getValue1() == words.length) { //sprawdzam czy wartosc w dict dla danego pliku jest rowna dlugosci words
                result[i] = dict.getAppearedWordsWithCount()[i].getValue0();       //co rownowazne jest z tym ze zawiera wszystkie te slowa
            }
        }
        return trim(result);
    }

    /**
     * Zwraca n plików zawierających najwięcj poszukiwanych słów
    // * @param words
     * @return
     */
    public String[] getDocsWithMaxMatchingWords(String[] words, int n) {
        MDictionary dict = new MDictionary();
        //dodawanie do MDictionary
        for (File file : new File("files").listFiles()) {
            dict.Add(file.getName());
        }

        //Zwiekszanie ilosci wystapien w MDictionary czytajac z plikow indeksowych
        for (String word : words) {
            try {
                BufferedReader br = new BufferedReader(new FileReader("indices/" + word + ".txt"));
                String line;
                while((line = br.readLine()) != null) {
                    String[] splitArr = line.split("\t");
                    dict.Find(splitArr[0]);
                }
            } catch (IOException e) {}
        }

        //przepisuje na pozniej zwracana tablice typu String
        Pair<String, Integer>[] tmp = dict.getAppearedWordsWithCount();
        String[] tmp1 = new String[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            tmp1[i] = tmp[i].getValue1() + ". " + tmp[i].getValue0();
        }

        //Zlozonosc sorta O(n+n+nulle) = O(n) gdzie nulli jest tyle co words.length

        String[][] arrayOfcontainers = new String[words.length][tmp1.length]; //dwuwymiarowa tablica zawierajaca tablice o maksymalnej mozliwej ilosci elementow rownej ilosci elementow z naszego dict
        String[] result = new String[n];
        int[] arrayOfCounts = new int[words.length]; //tablica zliczajaca przesuniecia elementow w naszej tablicy bucket

        for (int i = 0; i < tmp1.length; i++) {
            int k = Integer.parseInt(tmp1[i].split(". ")[0]); //liczba wystapien slow w pliku
            arrayOfcontainers[k][arrayOfCounts[k]] = tmp1[i]; //arrayOfcontainers[k] czyli na indeksie wystapien pliku, arrayOfcontainers[k][arrayOfCounts[k]] wstawia plik na pozycji przesunietej o zliczony count
            arrayOfCounts[k]++; //zwiekszamy count dla naszego indexu;
        }

        int count12 = 0; //count zliczajacy dlugosc n
        int count13 = 0; //count zliczajacy elementy w podtablicach
        for (int j = arrayOfcontainers.length-1; j >= arrayOfcontainers.length - n; j--) {
            String[] k = arrayOfcontainers[j]; //podtablica na indeksie j liczone od tylu
            while (k[count13] != null && count12 < n) { //sprawdzamy czy w naszej podtablicy nastepny jest null co sprawia ze zachowujemy zlozonosc O(n) - przechodzimy n razy przez tablice i czy nie przekroczylismy n
                result[count12] = k[count13]; //do tablicy result wpisujemy nastepny element ktory nie jest nullem
                count12++;
                count13++;
            }
            count13 = 0; //zerujemy count podtablic aby moc ponownie iterowac od ich poczatku
        }

        return result;
    }

    /**
     * Zwrócenie n dokumentów z największą zgodnościa z wybranym profilem
     * @param n
     * @return
     */
    public Pair<String,Double>[] getDocsClosestToProfile(int n, String profileName) {
        MDictionary dict = new MDictionary();
        File revFile = new File("reversedIndices");

        Pair<String, Double>[] pairs = new Pair[new File("files").listFiles().length];

        String[] profileWords = readProfile(profileName + ".txt");
        for (String profileWord : profileWords) {
            dict.Add(profileWord);
        }

        //zliczam zgodnosc slowa z profilem liczac wartosci z plikow reversedIndexes
        for (int i = 0; i < revFile.listFiles().length; i++){
            double resZg = 0; //suma zgodnosci slowa z profilem
            try {
                BufferedReader br = new BufferedReader(new FileReader(revFile.listFiles()[i]));
                String line;
                while ((line = br.readLine()) != null){
                    String splitArr = line.split("\t")[0];
                    if (dict.Find(splitArr) > 0) {
                        int count = Integer.parseInt(line.split("\t")[1]);
                        double zg = Math.log10(count)*100; //zgodnosc slowa z profilem
                        resZg += zg;
                    }
                }
            } catch (IOException e){
                e.printStackTrace();
            }

            //formatowanie doubla
            String formatted = String.format("%.1f", resZg/profileWords.length);
            pairs[i] = new Pair<>(revFile.listFiles()[i].getName(), Double.parseDouble(formatted));
        }

        //heapsort

        String[] maxHeap = new String[pairs.length];
        String[] pairsFormatted = new String[pairs.length];

        for (int i = 0; i < maxHeap.length; i++) {
            pairsFormatted[i] = pairs[i].getValue0() + ": " + pairs[i].getValue1();
        }

        int size = 0;
        for (String e : pairsFormatted) {
            insertIntoMaxHeap(e, maxHeap, size);
            size++;
        }

        for (int i = maxHeap.length-1; i >= 0; i--) {
            heapify(maxHeap, i);
        }

        String[] resultStrings = new String[n];
        for (int i = resultStrings.length-1; i >= 0; i--) {
            resultStrings[i] = maxHeap[maxHeap.length-i-1];
        }

        Pair<String, Double>[] resultPairs = new Pair[resultStrings.length];
        for (int i = 0; i < resultStrings.length; i++) {
            resultPairs[i] = new Pair<String, Double>(resultStrings[i].split(": ")[0], Double.parseDouble(resultStrings[i].split(": ")[1]));
        }

        return resultPairs;
    }
    private void insertIntoMaxHeap(String s, String[] heap, int size) {
        String[] splitArr = s.split(": ");
        int parentIndex = (int)Math.floor((size-1)/2);
        heap[size] = s;
        int idx = size;

        while (Double.parseDouble(splitArr[1]) > Double.parseDouble(heap[parentIndex].split(": ")[1]))  {
            swap(heap, parentIndex, idx);
            idx = parentIndex;
            parentIndex = (int) Math.floor((idx - 1) / 2);
        }
    }

    private void heapify(String[] heap, int size) {
        swap(heap, 0, size);
        int idx = 0;
        int topChild = 0;

        while (true) {
            String leftChild = heap[2 * idx + 1];
            String rightChild = heap[2 * idx + 2];
            topChild = idx;

            if ((2 * idx + 1) < size  && Double.parseDouble(leftChild.split(": ")[1]) > Double.parseDouble(rightChild.split(": ")[1])) {
                topChild = 2 * idx + 1;
            }
            if ((2 * idx + 2) < size && Double.parseDouble(leftChild.split(": ")[1]) <= Double.parseDouble(rightChild.split(": ")[1])) {
                topChild = 2 * idx + 2;
            }

            if (topChild != idx) {
                swap(heap, idx, topChild);
                idx = topChild;
                if (2 * idx + 2 > size)
                    break;
            } else
                break;
        }
    }

    private void swap(String[] arr, int i, int j) {
        String tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    private Pair<String, Integer>[] readIndex(File file, int count) {
        return null;
    }

    private Pair<String, Integer>[] sort(Pair<String, Integer>[] pairs) {
        return null;
    }
    
    public String[] trim(String[] arr) {
        int size = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != null) {
                size++;
            }
        }
        String[] tmp = new String[size];
        int count = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != null)
                tmp[count++] = arr[i];
        }
        return tmp;
    }

    public void deleteFiles() {
        for (File e : new File("indices").listFiles())
            e.delete();
        if (new File("reversedIndices").exists())
            for (File e : new File("reversedIndices").listFiles())
                e.delete();
        new File("reversedIndices").delete();
    }
}
