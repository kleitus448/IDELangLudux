package LuduxLang;

import java.util.NoSuchElementException;

public class LuduxLinkedList {

    //Голова связного списка и размер связного списка
    private Element header; private int size;

    public LuduxLinkedList() {
        header = new Element(null, header, header);
        header.setPrevious(header);
        header.setNext(header);
    };

    //Описание одного элемента связного списка
    private class Element {

        //Данные + ссылки на предыдущий и следующий элемент
        Object value; Element previous; Element next;

        Element(Object newData) {this.value = null;}

        Element(Object newData, Element previous, Element next) {
            this.value = newData; this.next = next; this.previous = previous;
        }

        //Задать и получить значение
        Object getData() {return value;}
        void setData(Object value) {this.value = value;}

        //Взять предыдущий элемент
        Element getPrevious() {return previous;}
        void setPrevious(Element previous) {this.previous = previous;}

        //Взять следующий элемент
        Element getNext() {return next;}
        void setNext(Element next) {this.next = next;}

        @Override
        public String toString() {return value.toString();}
    }

    int size() {return size;}

    //Проверка на пустоту списка
    boolean isNonEmpty() {return size != 0;}

    //Дообавление элементов в конец списка
    public void add(Object value) {
        Element newElement = new Element(value, header.previous, header);
        newElement.previous.next = newElement;
        newElement.next.previous = newElement;
        size++;
    }
    
    //Добавление элементов в произвольную поизицию списка
    public void add(Object value, int index) {
        Element newElement = new Element(value, index == size ? header : entry(index), header);
        newElement.previous.next = newElement;
        newElement.next.previous = newElement;
        size++;
    }

    //Поиск элемента, перед которым производится вставка элемента
    private Element entry(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Индекс: " + index
                                              + "Размер: " + size);
        Element entryElement = header;
        if (index < (size >> 1)) for (int i = 0; i <= index; i++)
            entryElement = entryElement.next;
        else for (int i = 0; i <= index; i++)
            entryElement = entryElement.previous;

        return entryElement;
    }

    //Получить элемент по индексу
    private Element get(int index) {
        if (index >= size) throw new IndexOutOfBoundsException();
        int i = 0; Element element = header;
        while (i < index) {element = element.getNext(); i++;}
        return element;
    }

    //Получить элемент по значению
    public Element get(Object value) {
        Element element = header; int i = 0;
        while (!value.equals(element.value)) {
            if (i == size) throw new NoSuchElementException();
            else {element = element.getNext(); i++;}
        }
        return element;
    }

    //Проверка элемента на наличие в списке
    public Boolean contains(Object value) {
        Element element = header; int i = 0;
        while (!value.equals(element.value)) {
            if (i == size) return false;
            else {element = element.getNext(); i++;}
        }
        return true;
    }

    //Удалить элемент по индексу
    public void remove(int index) {
        Element element = get(index);
        element.getPrevious().setNext(element.getNext());
        element.getNext().setPrevious(element.getPrevious());
    }

    //Удалить элемент по значению
    public void remove(Object value) {
        Element element = get(value);
        element.getPrevious().setNext(element.getNext());
        element.getNext().setPrevious(element.getPrevious());
    }

    //Последний элемент с удалением
    public Object poll() {
        if (size == 0) throw new NoSuchElementException();
        Object value = get(size-1).getData();
        remove(size-1);
        return value;
    }

    //Последний элемент без удаления
    public Object peek() {
        if (size == 0) throw new NoSuchElementException();
        return get(size-1).getData();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LuduxLinkedList list = (LuduxLinkedList) obj;
        for (int i = 0; i < size(); i++) {
            if (!get(i).equals(list.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("[");
        Element element = header.getNext();
        while (element.value != null) {
            string.append(element.toString());
            if (element.getNext().value != null) string.append(", ");
            System.out.println(element.value);
            element = element.getNext();
        }
        return string.append("]").toString();
    }

}
