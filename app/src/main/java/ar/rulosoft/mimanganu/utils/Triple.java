package ar.rulosoft.mimanganu.utils;

public class Triple<R, A, G> {

    private R first; //first member of pair
    private A second; //second member of pair
    private G third; //second member of pair

    public Triple(R first, A second, G third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public R getFirst() {
        return first;
    }

    public void setFirst(R first) {
        this.first = first;
    }

    public A getSecond() {
        return second;
    }

    public void setSecond(A second) {
        this.second = second;
    }

    public G getThird() {
        return third;
    }

    public void setThird(G third) {
        this.third = third;
    }
}
