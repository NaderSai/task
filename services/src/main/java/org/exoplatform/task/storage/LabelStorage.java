package org.exoplatform.task.storage;

import java.util.List;

import org.exoplatform.task.domain.Label;
import org.exoplatform.task.dto.LabelDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.exception.EntityNotFoundException;

public interface LabelStorage {

  List<LabelDto> findLabelsByUser(String username, int offset, int limit);

  List<LabelDto> findLabelsByTask(long taskId, String username, int offset, int limit);

  LabelDto getLabel(long labelId);

  LabelDto createLabel(LabelDto label);

  LabelDto updateLabel(LabelDto label, List<Label.FIELDS> fields) throws EntityNotFoundException;

  void removeLabel(long labelId);

  void addTaskToLabel(TaskDto task, Long labelId) throws EntityNotFoundException;

  void removeTaskFromLabel(TaskDto task, Long labelId) throws EntityNotFoundException;

  Label labelToEntity(LabelDto labelDto);

  Label mappingLabelToEntity(LabelDto labelDto);

  LabelDto labelToDto(Label Label);
}
