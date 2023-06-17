package ru.practicum.ewm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.converter.CompilationConverter;
import ru.practicum.ewm.model.dto.CompilationDto;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.model.CompilationModel;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.util.PageHelper;

import java.util.ArrayList;
import java.util.List;

@Service
public class CompilationService {

    private final CompilationRepository compilationRepository;

    @Autowired
    public CompilationService(CompilationRepository compilationRepository) {
        this.compilationRepository = compilationRepository;
    }

    public CompilationDto addCompilation(CompilationDto compilationDto) {
        var create = CompilationConverter.convToModel(compilationDto);
        var after = compilationRepository.save(create);
        return CompilationConverter.convToDto(after);
    }

    public CompilationDto updateCompilation(Long compId, CompilationDto compilationDto) {
        var check = compilationRepository.findById(compId);
        if (check.isEmpty()) {
            throw new NotFoundException("Compilation with id=" + compId + " was not found");
        }
        CompilationModel updatedComp = check.get();
        if (compilationDto.getTitle() != null) {
            updatedComp.setTitle(compilationDto.getTitle());
        }
        if (compilationDto.getPinned() != null) {
            updatedComp.setPinned(compilationDto.getPinned());
        }
        if (compilationDto.getEvents() != null) {
            updatedComp.setEvents(compilationDto.getEvents());
        }
        var after = compilationRepository.save(updatedComp);
        return CompilationConverter.convToDto(after);
    }

    public void deleteCompilation(Long compId) {
        compilationRepository.deleteById(compId);
    }

    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        if (pinned) {
            PageRequest pageRequest = PageHelper.createRequest(from, size);
            var CompPinned = compilationRepository.findAllByPinned(pageRequest, true).getContent();
            if (CompPinned.size() == 0) {
                return new ArrayList<>();
            }
            return CompilationConverter.mapToDto(CompPinned);

        } else {
            PageRequest pageRequest = PageHelper.createRequest(from, size);
            var allComp = compilationRepository.findAll(pageRequest).getContent();
            if (allComp.size() == 0) {
                return new ArrayList<>();
            }
            return CompilationConverter.mapToDto(allComp);
        }
    }

    public CompilationDto getCompilationsById(Long compId) {
        var result = compilationRepository.findById(compId);
        if (result.isEmpty()) {
            throw new NotFoundException("Compilation with id=" + compId + " was not found");
        }

        return CompilationConverter.convToDto(result.get());
    }
}