package org.springframework.samples.petclinic.service;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.mapper.UserMapper;
import org.springframework.samples.petclinic.model.Role;
import org.springframework.samples.petclinic.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  @Autowired private UserMapper userMapper;

  @Override
  @Transactional
  public void saveUser(User user) throws Exception {

    if (user.getRoles() == null || user.getRoles().isEmpty()) {
      throw new Exception("User must have at least a role set!");
    }

    for (Role role : user.getRoles()) {
      if (!role.getName().startsWith("ROLE_")) {
        role.setName("ROLE_" + role.getName());
      }

      if (role.getUser() == null) {
        role.setUser(user);
      }
    }

    userMapper.save(user);
  }
}
