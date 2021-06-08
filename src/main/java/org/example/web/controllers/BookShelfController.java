package org.example.web.controllers;

import org.apache.log4j.Logger;
import org.example.app.services.BookService;
import org.example.web.dto.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/books")
public class BookShelfController {

    private Logger logger = Logger.getLogger(BookShelfController.class);
    private BookService bookService;

    @Autowired
    public BookShelfController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/shelf")
    public String books(Model model,
                        @RequestParam(value = "size", required = false) Integer size,
                        @RequestParam(value = "author", required = false) String author,
                        @RequestParam(value = "title", required = false) String title) {
        logger.info("got book shelf");
        model.addAttribute("book", new Book());
        logger.info("got filter params: size = \"" + size + "\" author = \"" + author + "\" title = "+ title + "\"");

        // объект для фильтрации по регулярке
        Book filterBook = null;
        if (size != null || author != null || title != null) {
            filterBook = new Book();
            filterBook.setAuthor(author);
            filterBook.setSize(size);
            filterBook.setTitle(title);
        }
        model.addAttribute("bookList", bookService.getBooksFiltered(filterBook));
        return "book_shelf";
    }

    @PostMapping("/save")
    public String saveBook(Book book) {
        // проверим книгу на незаполненные поля
        Boolean isNotEmpty = !book.getAuthor().isEmpty() ||
                !book.getTitle().isEmpty() ||
                book.getSize() != null;
        if (isNotEmpty) {
            bookService.saveBook(book);
            logger.info("current repository size: " + bookService.getAllBooks().size());
        } else {
            logger.warn("cannot save empty book");
        }
        return "redirect:/books/shelf";
    }

    @PostMapping("/remove")
    public String removeBook(@RequestParam(value = "bookIdToRemove") String bookIdToRemove) {
        // залипуха, чтобы можно было большие id вводить
        Integer idToRemove = null;
        try {
            idToRemove = Integer.parseInt(bookIdToRemove);
        } catch (NumberFormatException nfe) {
            idToRemove = 0;
        }
        bookService.removeBookById(idToRemove);
        return "redirect:/books/shelf";
    }

    @PostMapping("/remove-by-expr")
    public String removeBooksByExpr(Book book) {
        logger.info("got filter book for remove: " + book);
        // найдем по переданным данным все книги на удаление, и удалим их
        List<Book> booksToBeDeleted = bookService.getBooksFiltered(book);
        booksToBeDeleted.stream().forEach(b -> {
            logger.info("remove book " + b);
            bookService.removeBookById(b.getId());
        });
        return "redirect:/books/shelf";
    }
    @PostMapping("/filter-by-expr")
    public String filterBooksByExpr(@RequestBody String body) {
        // кажется, здесь очень плохой костыль
        // TO BE - изучить и поискать способ нормально фильтровать в одном интерфейсе
        logger.info("got filter params: " + body);
        return String.format("redirect:/books/shelf?%s", body);
    }
}
