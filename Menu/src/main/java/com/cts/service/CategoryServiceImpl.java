package com.cts.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cts.dto.CategoryDto;
import com.cts.entity.Category;
import com.cts.exception.CategoryNotFoundException;
import com.cts.exception.DuplicateException;
import com.cts.repository.CategoryRepository;

@Service

public class CategoryServiceImpl implements CategoryService {
	private final CategoryRepository categoryRepository;

	public CategoryServiceImpl(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	@Override
	public List<CategoryDto> getAllCategories() {
		return categoryRepository.findAll().stream().map(this::convertEntityToDto).collect(Collectors.toList());
	}

	@Override
	public CategoryDto getCategoryById(Long id) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + id));
		return convertEntityToDto(category);
	}

	@Override
	public CategoryDto createCategory(CategoryDto dto) {
		boolean exists=categoryRepository.existsByCategoryName(dto.getCategoryName());
		if(exists) {
			throw new DuplicateException("Category is already present");
		}
		Category category = convertDtoToEntity(dto);
		Category saved = categoryRepository.save(category);
		return convertEntityToDto(saved);
	}

	@Override
	public CategoryDto updateCategory(Long id, CategoryDto dto) {
		Category existing = categoryRepository.findById(id)
				.orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + id));

		existing.setCategoryName(dto.getCategoryName());
		existing.setDescription(dto.getDescription());

		Category updated = categoryRepository.save(existing);
		return convertEntityToDto(updated);
	}

	@Override
	public void deleteCategory(Long id) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + id));
		categoryRepository.delete(category);
	}

	private CategoryDto convertEntityToDto(Category category) {
		return new CategoryDto(category.getCategoryId(), category.getCategoryName(), category.getDescription());
	}

	private Category convertDtoToEntity(CategoryDto dto) {
		Category category = new Category();
		category.setCategoryName(dto.getCategoryName());
		category.setDescription(dto.getDescription());
		return category;
	}

	@Override
	public CategoryDto getCategoryByName(String name) {
		// TODO Auto-generated method stub
		Category c = categoryRepository.findByCategoryName(name)
				.orElseThrow(() -> new CategoryNotFoundException("Category with name" + name + "not found"));
		return convertEntityToDto(c);

	}
}
