package org.example.web.controllers;

import org.apache.log4j.Logger;
import org.example.app.services.BookService;
import org.example.web.dto.Book;
import org.example.web.dto.BookIdToRemove;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping(value = "/books")
public class BookShelfController {

    private final Logger logger = Logger.getLogger(BookShelfController.class);
    private final BookService bookService;

    /**
     * Возвращает подготовленный book_shelf
     * в него надо добавить некоторые объекты - они представлены параметрами
     * если в параметр передать null - будет использовано значение по-умолчанию (кроме модели)
     */
    private String prepareBookShelf(Model model,
                                    Book book,
                                    BookIdToRemove toRemove,
                                    Book toRemoveByExpr,
                                    Book toFilterByExpr,
                                    List<Book> bookList) {
        if (book == null) {
            model.addAttribute("book", new Book());
        } else {
            model.addAttribute("book", book);
        }
        if (toRemove == null) {
            model.addAttribute("bookIdToRemove", new BookIdToRemove());
        } else {
            model.addAttribute("bookIdToRemove", toRemove);
        }
        if (toRemoveByExpr == null) {
            model.addAttribute("bookToRemoveByExpr", new Book());
        } else {
            model.addAttribute("bookToRemoveByExpr", toRemoveByExpr);
        }
        if (toFilterByExpr == null) {
            model.addAttribute("bookToFilterByExpr", new Book());
        } else {
            model.addAttribute("bookToFilterByExpr", toFilterByExpr);
        }
        if (bookList == null) {
            model.addAttribute("bookList", bookService.getAllBooks());
        } else {
            model.addAttribute("bookList", bookList);
        }
        return "book_shelf";
    }

    @Autowired
    public BookShelfController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/shelf")
    public String books(Model model) {
        logger.info("got book shelf");
        return prepareBookShelf(model, null, null, null, null, null);
    }

    @PostMapping("/save")
    public String saveBook(@Valid Book book,
                           BindingResult bindingResult,
                           Model model,
                           @ModelAttribute("bookList") List<Book> bookList) {
        if (bindingResult.hasErrors()) {
            logger.warn("cannot save - errors in book found");
            return prepareBookShelf(model,
                    (Book) bindingResult.getModel().get("book"),
                    (BookIdToRemove) bindingResult.getModel().get("bookIdToRemove"),
                    (Book) bindingResult.getModel().get("bookToRemoveByExpr"),
                    (Book) bindingResult.getModel().get("bookToFilerByExpr"),
                    bookList);
        } else {
            bookService.saveBook(book);
            return "redirect:/books/shelf";
        }
    }

    @PostMapping("/remove")
    public String removeBook(@Valid BookIdToRemove bookIdToRemove,
                             BindingResult bindingResult,
                             Model model,
                             @ModelAttribute("bookList") List<Book> bookList) {
        if (bindingResult.hasErrors()) {
            return prepareBookShelf(model,
                    (Book) bindingResult.getModel().get("book"),
                    (BookIdToRemove) bindingResult.getModel().get("bookIdToRemove"),
                    (Book) bindingResult.getModel().get("bookToRemoveByExpr"),
                    (Book) bindingResult.getModel().get("bookToFilerByExpr"),
                    bookList);
        } else {
            bookService.removeBookById(bookIdToRemove.getId());
            return "redirect:/books/shelf";
        }
    }

    @PostMapping("/remove-by-expr")
    public String removeBooksByExpr(@ModelAttribute("bookToRemoveByExpr") Book book) {
        logger.info("got filter book for remove: " + book);
        // удалим книги по выражению
        bookService.removeBookByFilter(book);
        return "redirect:/books/shelf";
    }
    @PostMapping("/filter-by-expr")
    public String filterBooksByExpr(Model model, @ModelAttribute("bookToFilterByExpr") Book filterBook) {
        logger.info("got filter params: " + filterBook);
        return prepareBookShelf(model,
                (Book) model.getAttribute("book"),
                (BookIdToRemove) model.getAttribute("bookIdToRemove"),
                (Book) model.getAttribute("bookToRemoveByExpr"),
                filterBook,
                bookService.getBooksFiltered(filterBook));
    }
}
