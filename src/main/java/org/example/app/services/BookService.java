package org.example.app.services;

import org.apache.log4j.Logger;
import org.example.web.dto.Book;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
public class BookService implements DisposableBean, BeanPostProcessor {

    private final ProjectRepository<Book> bookRepo;
    private final Logger logger = Logger.getLogger(BookService.class);

    @Autowired
    public BookService(ProjectRepository<Book> bookRepo) {
        this.bookRepo = bookRepo;
    }

    public List<Book> getAllBooks() {
        return bookRepo.retrieveAll();
    }

    public void saveBook(Book book) {
        // проверим книгу на незаполненные поля
        boolean isNotEmpty = !book.getAuthor().isEmpty() ||
                !book.getTitle().isEmpty() ||
                book.getSize() != null;
        if (isNotEmpty) {
            logger.info("current repository size: " + getAllBooks().size());
            bookRepo.store(book);
        } else {
            logger.warn("cannot save empty book");
        }
    }

    public boolean removeBookById(Integer bookIdToRemove) {
        boolean isDeleted = bookRepo.removeItemById(bookIdToRemove);
        if (isDeleted) {
            logger.info("deleted book with id " + bookIdToRemove);
        } else {
            logger.warn("cannot delete book with id " + bookIdToRemove);
        }
        return isDeleted;
    }

    public List<Book> getBooksFiltered(Book filterBook) {
        if (filterBook == null) {
            return getAllBooks();
        }
        // строковые поля для поиска по регулярному выражению, чтобы не менять исходный объект
        final String title = (filterBook.getTitle() == null || "".equals(filterBook.getTitle())) ? ".*" : filterBook.getTitle();
        final String author = (filterBook.getAuthor() == null || "".equals(filterBook.getAuthor())) ? ".*" : filterBook.getAuthor();
        final String filteredBookSize = filterBook.getSize() != null ? filterBook.getSize().toString() : ".*";

        // проверяем каждую книгу, обращаем внимание, что у ее полей может быть значение null
        return bookRepo.retrieveAll()
                .stream()
                .filter(book -> {
                    BiFunction<String, String, Boolean> compareWithNull = (s1, s2) ->
                            (s1 == null & ".*".equals(s2)) || (s1 != null && s1.matches(s2));
                    String bookSize = null;
                    if (book.getSize() != null) {
                        bookSize = book.getSize().toString();
                    }
                    return compareWithNull.apply(book.getAuthor(), author)
                            & compareWithNull.apply(book.getTitle(), title)
                            & compareWithNull.apply(bookSize, filteredBookSize);

                }
                ).collect(Collectors.toList());
    }

    public void removeBookByFilter(Book book) {
        // найдем по переданным данным все книги на удаление, и удалим их
        List<Book> booksToBeDeleted = getBooksFiltered(book);
        booksToBeDeleted.forEach(b -> removeBookById(b.getId()));
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        logger.info("invoked postProcessBeforeInitialization for " + beanName);
        return null;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        logger.info("invoked postProcessAfterInitialization for " + beanName);
        return null;
    }

    @Override
    public void destroy() {
        logger.info("invoked destroy from DisposableBean");
    }
}
