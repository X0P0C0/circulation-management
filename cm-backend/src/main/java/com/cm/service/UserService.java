package com.cm.service;

import com.cm.common.result.PageResult;
import com.cm.dto.UserDTO;
import com.cm.vo.UserVO;
import java.util.List;

public interface UserService {
    List<UserVO> listAll();
    PageResult<UserVO> listPage(String keyword, String sortFields, String sortOrders,
                               Integer pageNum, Integer pageSize);
    UserVO create(UserDTO dto);
    UserVO update(Long id, UserDTO dto);
    void delete(Long id);
    void toggleStatus(Long id);
}
