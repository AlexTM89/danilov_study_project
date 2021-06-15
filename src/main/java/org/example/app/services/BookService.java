package org.example.app.services;

import org.apache.log4j.Logger;
import org.example.web.dto.Book;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Scope;
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

    public boolean removeBookById(String bookIdToRemove) {
        boolean isDeleted =bookRepo.removeItemById(bookIdToRemove);
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
        // чтобы пустые поля фильтра не влияли на поиск - заменим их на регулярку, которая не будет на него влиять
        if (filterBook.getTitle() == null || "".equals(filterBook.getTitle())) {
            filterBook.setTitle(".*");
        }
        if (filterBook.getAuthor() == null || "".equals(filterBook.getAuthor())) {
            filterBook.setAuthor(".*");
        }
        String filteredBookSize;
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
                    BiFunction<String, String, Boolean> compareWithNull = (s1, s2) ->
                            (s1 == null & ".*".equals(s2)) || (s1 != null && s1.matches(s2));
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
    public void destroy() throws Exception {
        logger.info("invoked destroy from DisposableBean");
    }
}
