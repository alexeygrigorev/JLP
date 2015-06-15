package jlp;

public class GenericTest<E> {

    public <G> void method(E p1, G p2) {
        System.out.println(p1);
        System.out.println(p2);
    }

}
