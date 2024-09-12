package com.techlibrary.houseofbooks.services;

import com.techlibrary.houseofbooks.dto.LoanDTO;
import com.techlibrary.houseofbooks.entities.Book;
import com.techlibrary.houseofbooks.entities.Loan;
import com.techlibrary.houseofbooks.entities.User;
import com.techlibrary.houseofbooks.repositories.BookRepository;
import com.techlibrary.houseofbooks.repositories.LoanRepository;
import com.techlibrary.houseofbooks.repositories.UserRepository;
import com.techlibrary.houseofbooks.services.exceptions.BookBorrowedException;
import com.techlibrary.houseofbooks.services.exceptions.BookIsNotBorrowedException;
import com.techlibrary.houseofbooks.services.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public LoanService(LoanRepository loanRepository, BookRepository bookRepository, UserRepository userRepository) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    public LoanDTO insertLoan(LoanDTO dto) {

        Book bookEntity = bookRepository.findById(dto.getIdBook())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + dto.getIdBook()));

        if (Boolean.TRUE.equals(bookEntity.getBorrowed())) {
            throw new BookBorrowedException("Book is already borrowed.");
        } else {
            bookEntity.setBorrowed(true);
            bookRepository.save(bookEntity);
        }

        Optional<User> objUser = userRepository.findById(dto.getIdUser());
        User userEntity = objUser.orElseThrow(() -> new ResourceNotFoundException("User not Found"));

        Loan loanEntity = new Loan(bookEntity, userEntity);
        loanRepository.save(loanEntity);

        return new LoanDTO(loanEntity);
    }

    public void returnBook(Long id) {
        Optional<Book> optionalBook = bookRepository.findById(id);
        Book bookEntity = optionalBook.orElseThrow(() -> new ResourceNotFoundException("Book not Found"));
        if (Boolean.TRUE.equals(bookEntity.getBorrowed()) && loanRepository.existsByBookId(bookEntity.getId())) {

            Optional<Loan> optionalLoan = loanRepository.findByBookId(bookEntity.getId());
            Loan loanEntity = optionalLoan.orElseThrow(() -> new ResourceNotFoundException("Loan not Found"));

            loanRepository.deleteById(loanEntity.getId());
            bookEntity.setBorrowed(false);
            bookRepository.save(bookEntity);
        } else {
            throw new BookIsNotBorrowedException("the book is not on loan");
        }
    }
}
