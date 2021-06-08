package org.example.app.services;

import org.example.web.dto.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final ProjectRepository<Book> bookRepo;

    @Autowired
    public BookService(ProjectRepository<Book> bookRepo) {
        this.bookRepo = bookRepo;
    }

    public List<Book> getAllBooks() {
        return bookRepo.retrieveAll();
    }

    public void saveBook(Book book) {
        bookRepo.store(book);
    }

    public boolean removeBookById(Integer bookIdToRemove) {
        return bookRepo.removeItemById(bookIdToRemove);
    }

    public List<Book> getBooksFiltered(Book filterBook) {
        if (filterBook == null) {
            return getAllBooks();
        }
        if (filterBook.getTitle() == null || filterBook.getTitle() == "") {
            filterBook.setTitle(".*");
        }
        if (filterBook.getAuthor() == null || filterBook.getAuthor() == "") {
            filterBook.setAuthor(".*");
        }
        String filteredBookSize = null;
        if (filterBook.getSize() != null) {
            filteredBookSize = filterBook.getSize().toString();
        } else {
            filteredBookSize = ".*";
        }
        // проверяем каждую книгу, обращаем внимание, что у ее полей может быть значение null
        String finalFilteredBookSize = filteredBookSize;
        return bookRepo.retrieveAll()
                .stream()
                .filter(book -> {
                    BiFunction<String, String, Boolean> compareWithNull = (s1, s2) -> {
                        return (s1 == null & ".*".equals(s2)) || (s1 != null && s1.matches(s2));
                    };
                    String bookSize = null;
                    if (book.getSize() != null) {
                        bookSize = book.getSize().toString();
                    }
                    return compareWithNull.apply(book.getAuthor(), filterBook.getAuthor())
                            & compareWithNull.apply(book.getTitle(), filterBook.getTitle())
                            & compareWithNull.apply(bookSize, finalFilteredBookSize);

                }
                ).collect(Collectors.toList());
    }
}
