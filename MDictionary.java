/**
 * !!! Podlegać modyfikacji mogę jedynie elementy oznaczone to do. !!!
 */

import org.javatuples.Pair;

import java.util.HashMap;
import java.util.Map;

public class MDictionary {
    Pair<String, Integer>[] wordsWithCount;
    int size;
    public MDictionary() {
        this.wordsWithCount = new Pair[Rozmiar.MAX_ELEM];
        this.size = 0;
    }

    /**
     Opróżnia słownik i zwalnia pamięć po kolekcjach słownikowych
     // <remarks>Metoda przydatna na zakończenie Dictu lub przed ponownym załadowaniem</remarks>
     **/
    public void Empty()
    {
        for (Pair<String, Integer> e : wordsWithCount) {
            e = new Pair<>(null, 0);
        }
    }

    /**
     Metoda zeruje liczbę wystąpień pojęć w słowniku
    **/
    public void Reset()
    {
        for (int i = 0; i < wordsWithCount.length; i++) {
            if (wordsWithCount[i] != null) {
                wordsWithCount[i] = new Pair<>(wordsWithCount[i].getValue0(), 0);
            }
        }
    }

    /**
    Dodanie pojęcia do słownika na podstawie słowa i numeru klucza haszowego
    **/
    private int Add(String W, int h)
    {
        if (wordsWithCount[h] == null) {
            wordsWithCount[h] = new Pair<>(W, 0);
            size++;
        }
        return -1;
    }

    /**
    /// Dodanie pojęcia do słownika na podstawie słowa
    **/
    public int Add(String W)
    {
        return Add(W, Haszuj(W));
    }

    /**
     Podaje klucz dla danego słowa
    **/
    private int Haszuj(String W)
    {
        int hash = 0;
        for (int i = 0; i < W.length(); i++) {
            hash += (int)W.charAt(i);
        }
        hash %= Rozmiar.MAX_ELEM;
        while (wordsWithCount[hash] != null) {
            if (wordsWithCount[hash].getValue0().equals(W)) {
                break;
            } else {
                hash += 2;
                hash %= Rozmiar.MAX_ELEM;
            }
        }
        return hash % Rozmiar.MAX_ELEM;
    }

    /**
    Metoda zwraca numer słowa lub 0 i zwiększa liczbę wystąpień
    **/
    private int Find(String W, int h)
    {
        if (wordsWithCount[h] != null) {
            int tmp = wordsWithCount[h].getValue1();
            wordsWithCount[h] = new Pair<>(W, tmp+1);
            return h;
        }
        return -1;
    }

    /**
     Metoda zwraca numer słowa lub 0 i zwiększa liczbę wystąpień o n
     **/
    public int FindAndAdd(String W, int n)
    {
        if (Find(W) != -1) {
            int h = Haszuj(W);
            int tmp = wordsWithCount[h].getValue1();
            wordsWithCount[h] = new Pair<>(W, tmp+n-1);
            return h;
        }
        return -1;
    }

    /** <summary>
    Metoda zwraca numer słowa lub 0 i zwiększa liczbę wystąpień
    **/
    public int Find(String W)
    {
        return Find(W, Haszuj(W));
    }

    /**
     * Zwraca słowa w słowniku
     */
    public String[] getWords() {
        String[] result = new String[size];
        int index = 0;
        for (Pair<String, Integer> pair : wordsWithCount) {
            if (pair != null) {
                result[index] = pair.getValue0();
                index++;
            }
        }
        return result;
    }

    /**
     * Zwraca słowa, które wystąpiły w dokumencie
     * @return
     */
    public String[] getAppearedWords() {
        String[] result = new String[size];
        int index = 0;
        for (Pair<String, Integer> pair : wordsWithCount) {
            if (pair != null) {
                result[index] = pair.getValue0();
                index++;
            }
        }
        return result;
    }

    /**
     * Zwraca pojęcia, które wystąpiły oraz liczba wystąpień
     * @return
     */
    public Pair<String, Integer>[] getAppearedWordsWithCount() {
        Pair<String, Integer>[] result = new Pair[size];
        int index = 0;
        for (Pair<String, Integer> pair : wordsWithCount) {
            if (pair != null) {
                result[index] = pair;
                index++;
            }
        }
        return result;
    }

}
