package com.cm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cm.common.result.PageResult;
import com.cm.common.util.SortUtils;
import com.cm.common.exception.BusinessException;
import com.cm.dto.UserDTO;
import com.cm.entity.User;
import com.cm.enums.RoleEnum;
import com.cm.mapper.UserMapper;
import com.cm.service.UserService;
import com.cm.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public List<UserVO> listAll() {
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>().orderByAsc(User::getId));
        return users.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public PageResult<UserVO> listPage(String keyword, String sortFields, String sortOrders,
                                       Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword));
        }
        Map<String, SFunction<User, ?>> sortMapper = new HashMap<>();
        sortMapper.put("id", User::getId);
        sortMapper.put("username", User::getUsername);
        sortMapper.put("realName", User::getRealName);
        sortMapper.put("status", User::getStatus);
        sortMapper.put("createTime", User::getCreateTime);
        SortUtils.applyMultiSort(wrapper, sortFields, sortOrders, sortMapper, User::getId, true);
        Page<User> page = userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<UserVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(records, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public UserVO create(UserDTO dto) {
        // 检查用户名是否已存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException(400, "用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setRole(dto.getRole() != null ? dto.getRole() : RoleEnum.OPERATOR.getCode());
        user.setStatus(1);
        userMapper.insert(user);
        return toVO(user);
    }

    @Override
    public UserVO update(Long id, UserDTO dto) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (dto.getRealName() != null) user.setRealName(dto.getRealName());
        if (dto.getRole() != null) user.setRole(dto.getRole());
        // 如果提供了新密码则更新
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        userMapper.updateById(user);
        return toVO(user);
    }

    @Override
    public void delete(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        // 不允许删除最后一个管理员
        if (user.getRole() == RoleEnum.ADMIN.getCode()) {
            Long adminCount = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getRole, RoleEnum.ADMIN.getCode()));
            if (adminCount <= 1) {
                throw new BusinessException(400, "不能删除最后一个管理员");
            }
        }
        userMapper.deleteById(id);
    }

    @Override
    public void toggleStatus(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        userMapper.updateById(user);
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        vo.setRoleName(RoleEnum.of(user.getRole()).getDesc());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
