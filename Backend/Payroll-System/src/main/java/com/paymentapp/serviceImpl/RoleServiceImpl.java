package com.paymentapp.serviceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.paymentapp.dto.RoleRequestDTO;
import com.paymentapp.dto.RoleResponseDTO;
import com.paymentapp.entity.Role;
import com.paymentapp.repository.RoleRepository;
import com.paymentapp.service.RoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;

    @Override
    public RoleResponseDTO createRole(RoleRequestDTO dto) {
        Role role = modelMapper.map(dto, Role.class);
        Role savedRole = roleRepository.save(role);
        return modelMapper.map(savedRole, RoleResponseDTO.class);
    }

    @Override
    public RoleResponseDTO getRoleById(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        return modelMapper.map(role, RoleResponseDTO.class);
    }

    @Override
    public List<RoleResponseDTO> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(role -> modelMapper.map(role, RoleResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoleResponseDTO updateRole(Long roleId, RoleRequestDTO dto) {
        Role existingRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        existingRole.setRoleName(dto.getRoleName());
        Role updatedRole = roleRepository.save(existingRole);
        return modelMapper.map(updatedRole, RoleResponseDTO.class);
    }

    @Override
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        roleRepository.delete(role);
    }
}
