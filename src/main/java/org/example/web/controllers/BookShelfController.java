package org.example.web.controllers;

import org.apache.log4j.Logger;
import org.example.app.exceptions.UploadNullFileException;
import org.example.app.services.BookService;
import org.example.app.services.FileService;
import org.example.web.dto.Book;
import org.example.web.dto.BookIdToRemove;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/books")
public class BookShelfController {

    private final Logger logger = Logger.getLogger(BookShelfController.class);
    private final BookService bookService;
    private final FileService fileService;

    // вспомогательная функция вытаскивания из модели атрибута
    private <T> T getModelObject(Model model, String param) {
        try {
            if (model.containsAttribute(param)) {
                // небезопасно, но для учебного проекта - самое оно, код уменьшает хорошо
                return (T) model.getAttribute(param);
            } else {
                return null;
            }
        } catch (RuntimeException e) {
           return null;
        }
    }

    /**
     * Возвращает подготовленный book_shelf
     * в него надо добавить некоторые объекты - они представлены параметрами
     * если в параметр передать null - будет использовано значение по-умолчанию (кроме модели)
     */
    public String prepareBookShelf(Model model) {
        Book book = getModelObject(model, "book");
        if (book == null) {
            book = new Book();
        }

        BookIdToRemove bookIdToRemove = getModelObject(model, "bookIdToRemove");
        if (bookIdToRemove == null) {
            bookIdToRemove = new BookIdToRemove();
        }

        Book bookToRemoveByExpr = getModelObject(model, "bookToRemoveByExpr");
        if (bookToRemoveByExpr == null) {
            bookToRemoveByExpr = new Book();
        }

        Book bookToFilterByExpr = getModelObject(model, "bookToFilterByExpr");
        if (bookToFilterByExpr == null) {
            bookToFilterByExpr = new Book();
        }

        List<Book> bookList = getModelObject(model, "bookList");
        if (bookList == null) {
            bookList = bookService.getAllBooks();
        }

        model.addAttribute("book", book);
        model.addAttribute("bookIdToRemove", bookIdToRemove);
        model.addAttribute("bookToRemoveByExpr", bookToRemoveByExpr);
        model.addAttribute("bookToFilterByExpr", bookToFilterByExpr);
        model.addAttribute("bookList", bookList);

        return "book_shelf";
    }

    @Autowired
    public BookShelfController(BookService bookService,
                               FileService fileService) {
        this.bookService = bookService;
        this.fileService = fileService;
    }

    @GetMapping("/shelf")
    public String books(Model model) {
        logger.info("got book shelf");
        return prepareBookShelf(model);
    }

    @PostMapping("/save")
    public String saveBook(@Valid Book book,
                           BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            logger.warn("cannot save - errors in book found");
            return prepareBookShelf(model);
        } else {
            bookService.saveBook(book);
            return "redirect:/books/shelf";
        }
    }

    @PostMapping("/remove")
    public String removeBook(@Valid BookIdToRemove bookIdToRemove,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            // определим из модели список атрибутов
            return prepareBookShelf(model);
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
    public String filterBooksByExpr(Model model,
                                    @ModelAttribute("bookToFilterByExpr") Book filterBook) {
        logger.info("got filter params: " + filterBook);
        model.addAttribute("bookList", bookService.getBooksFiltered(filterBook));
        return prepareBookShelf(model);
    }

    @PostMapping(value = "/upload-file")
    public String uploadFile(@RequestParam("file") MultipartFile file) throws UploadNullFileException, IOException {
        logger.info("starting upload with file named '" + file.getName() + "'");

        String fileName = file.getOriginalFilename();
        Optional<String> fileNameOpt = Optional.ofNullable(fileName);
        if (!fileNameOpt.isPresent() || fileNameOpt.get().isEmpty() ) {
            throw new UploadNullFileException("cannot upload empty file");
        }
        byte[] fileContent = file.getBytes();

        // create dir
        String rootPath = System.getProperty("catalina.home");
        File dir = Paths.get(rootPath, "uploads").toFile();
        if (!dir.exists()) {
            logger.info("create upload directory");
            dir.mkdirs();
        }

        // create file
        try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(Paths.get(dir.getAbsolutePath(), fileName).toString()))) {
            stream.write(fileContent);
        }
        return "redirect:/books/shelf";
    }

    @GetMapping(value = "/files", produces = MediaType.TEXT_HTML_VALUE)
    public String listFiles(Model model) {
        model.addAttribute("fileList", fileService.getFiles());
        return "files_page";
    }

    @PostMapping(value = "/files")
    public void loadFile(@ModelAttribute("filename") String fileName,
                           HttpServletResponse response) {
        logger.info("get filename " + fileName);
        fileService.downloadFile(fileName, response);
    }

    @ExceptionHandler({IOException.class, UploadNullFileException.class})
    public String handleBookShelfExceptions(Model model, Exception e) {
        logger.info("handle exceptions: " + e.getMessage());
        model.addAttribute("errorMessage", e.getMessage());
        logger.info("goto error page 405");
        return "errors/405";
    }
}
