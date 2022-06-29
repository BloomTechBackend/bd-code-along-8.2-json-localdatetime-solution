package controllers;

import enums.BookState;
import exceptions.BookNotFoundException;
import exceptions.MaximumBookCheckedOutException;
import models.Book;
import models.Library;
import models.User;
import observer.BookStateObserver;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LibraryController {

    private final Library library = new Library();
    private List<BookStateObserver> observers = new ArrayList<>();

    public LibraryController() {
        loadBooks("books.csv");
    }
    
    /**
     * Checks a book out if it can. The book's state should be AVAILABLE. If it's not, return false.
     * Upon successful check-out, the book's state should be set to CHECKED_OUT.
     * @param book - the book to be checked out
     * @param user - the user that is checking out the book.
     * @return true if the book is checked out. Otherwise, false.
     */
    //TODO Step 1.2 - Finish checkoutBook
    public boolean checkoutBook(Book book) {
        if (book.getState() != BookState.AVAILABLE) {
            return false;
        }

        book.setState(BookState.CHECKED_OUT);
        updateObservers(book);
        return true;
    }

     /**
     * Returns the book if it can. The book's state should be CHECKED_OUT. If it's anything else, return false.
     * Upon successful check-in, the book's state should be set to CHECKED_IN.
     * @param book - the book being checked in
     * @return true if the book was successfully checked out
     */
    //TODO Step 1.3 - Finish returnBook
    public boolean checkInBook(Book book) {
        if (book.getState() != BookState.CHECKED_OUT) {
            return false;
        }

        book.setState(BookState.CHECKED_IN);
        updateObservers(book);
        return true;
    }

     /**
     * Goes through all the books and changes any state that's CHECKED_IN to AVAILABLE.
     */
    public void processCheckedInBooks() {
        for (Book book : getBooks()) {
            if (book.getState() == BookState.CHECKED_IN) {
                book.setState(BookState.AVAILABLE);
                updateObservers(book);
            }
        }
    }
    

    //Step 2.2 Finish add, remove, and updateObservers
    public void addObserver(BookStateObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(BookStateObserver observer) {
        observers.remove(observer);
    }

    private void updateObservers(Book book) {
        for (BookStateObserver observer : observers) {
            observer.onBookStatusChanged(book);
        }
    }

    
    
    
    
    
    
    
   public void loadBooks(String resourceName) {
        /**
         * NOTE: data in the CSV are in the following order:
         *       isbn, authors, publication year, title, average_rating
         */
        URL u = getClass().getClassLoader().getResource(resourceName);
        try {
            FileReader fReader = new FileReader(u.getFile());
            BufferedReader reader = new BufferedReader(fReader);

            String line = reader.readLine();
            while (line != null) {
                String[] data = line.split(",");
                addBook(new Book.Builder()
                        .isbn(data[0])
                        .author(data[1])
                        .publishedYear(data[2])
                        .title(data[3])
                        .rating(Float.parseFloat(data[4]))
                        .build()
                );
                line = reader.readLine();
            }

            fReader.close();
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void produceImportReport() {
        int[] ratingCounts = { 0, 0, 0, 0, 0};
        for (Book book : getBooks()) {
            int rating = Math.round(book.getRating());
            ratingCounts[rating - 1]++;
        }

        File report = new File("ratings_report.txt");
        try {
            FileOutputStream stream = new FileOutputStream(report);
            stream.write("Report\n".getBytes(StandardCharsets.UTF_8));
            for (int i = 0; i < ratingCounts.length; i++) {
                StringBuilder builder = new StringBuilder();
                builder.append(i + 1);
                builder.append("-star ratings: ");
                builder.append(ratingCounts[i]);
                builder.append("\n");
                stream.write(builder.toString().getBytes(StandardCharsets.UTF_8));
            }
            stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        DefaultPieDataset data = new DefaultPieDataset();

        data.setValue("1", ratingCounts[0]);
        data.setValue("2", ratingCounts[1]);
        data.setValue("3", ratingCounts[2]);
        data.setValue("4", ratingCounts[3]);
        data.setValue("5", ratingCounts[4]);

        JFreeChart chart = ChartFactory.createPieChart("Rating Count", data);
        try {
            ChartUtils.saveChartAsPNG(new File("chart.png"), chart, 400, 300);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void addBook(Book book) {
        library.addBook(book);
    }

    public Book[] getBooks() {
        return library.getBooks();
    }

}
