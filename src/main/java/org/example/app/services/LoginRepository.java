package org.example.app.services;

import org.example.web.dto.LoginForm;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class LoginRepository implements ProjectRepository<LoginForm> {

    private final List<LoginForm> repo = new ArrayList<>();
    {
        repo.add(new LoginForm("root", "123"));
    }

    @Override
    public List<LoginForm> retrieveAll() {
        return repo;
    }

    @Override
    public void store(LoginForm item) {
        repo.add(item);
    }

    @Override
    public boolean removeItemById(Integer itemToRemove) {
        // пока не удаляем
        return false;
    }
}
