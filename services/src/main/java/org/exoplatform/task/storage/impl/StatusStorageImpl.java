package org.exoplatform.task.storage.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.dao.StatusHandler;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.dto.ProjectDto;
import org.exoplatform.task.dto.StatusDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.exception.NotAllowedOperationOnEntityException;
import org.exoplatform.task.storage.ProjectStorage;
import org.exoplatform.task.storage.StatusStorage;
import org.exoplatform.task.storage.TaskStorage;

public class StatusStorageImpl implements StatusStorage {

  private static final Log     LOG     = ExoLogger.getExoLogger(StatusStorageImpl.class);

  private static final Pattern pattern = Pattern.compile("@([^\\s]+)|@([^\\s]+)$");

  @Inject
  private final DAOHandler     daoHandler;

  @Inject
  private final ProjectStorage projectStorage;
  private final TaskStorage taskStorage;

  public StatusStorageImpl(DAOHandler daoHandler, ProjectStorage projectStorage, TaskStorage taskStorage) {
    this.daoHandler = daoHandler;
    this.projectStorage = projectStorage;
    this.taskStorage = taskStorage;
  }

  @Override
  public StatusDto getStatus(long statusId) {
    return statusToDTO(daoHandler.getStatusHandler().find(statusId));
  }

  @Override
  public StatusDto getDefaultStatus(long projectId) {
    return statusToDTO(daoHandler.getStatusHandler().findLowestRankStatusByProject(projectId));
  }

  @Override
  public List<StatusDto> getStatuses(long projectId) {
    List<Status> statusDtos = daoHandler.getStatusHandler().getStatuses(projectId);
    return statusDtos.stream().map(this::statusToDTO).collect(Collectors.toList());
  }

  @Override
  public StatusDto createStatus(ProjectDto project, String status) {
    //
    List<StatusDto> statuses = getStatuses(project.getId());
    if (statuses != null) {
      for (StatusDto st : statuses) {
        if (st.getName().equalsIgnoreCase(status)) {
          LOG.warn("Status {} has already exists", status);
          return st;
        }
      }
    }

    StatusDto max = statusToDTO(daoHandler.getStatusHandler().findHighestRankStatusByProject(project.getId()));
    int maxRank = max != null && max.getRank() != null ? max.getRank() : -1;

    StatusHandler handler = daoHandler.getStatusHandler();
    Status st = new Status(status, ++maxRank, projectStorage.projectToEntity(project));
    handler.create(st);
    return statusToDTO(st);
  }

  @Override
  public StatusDto createStatus(ProjectDto project, String status, int rank) {
    //
    List<StatusDto> statuses = getStatuses(project.getId());
    if (statuses != null) {
      for (StatusDto st : statuses) {
        if (st.getName().equalsIgnoreCase(status)) {
          LOG.warn("Status {} has already exists", status);
          return st;
        }
      }
    }
    StatusHandler handler = daoHandler.getStatusHandler();
    Status st = new Status(status, rank, projectStorage.projectToEntity(project));
    handler.create(st);
    return statusToDTO(st);
  }

  @Override
  public void removeStatus(long statusId) throws Exception {
    StatusHandler handler = daoHandler.getStatusHandler();
    Status st = handler.find(statusId);
    if (st == null) {
      throw new EntityNotFoundException(statusId, Status.class);
    }

    Project project = st.getProject();
    Status altStatus = findAltStatus(st, project);
    if (altStatus == null) {
      throw new NotAllowedOperationOnEntityException(statusId, Status.class, "Delete last status");
    }
    List<Task> tasks = daoHandler.getTaskHandler().getByStatus(statusId);
    for(Task task : tasks){
      task.setStatus(altStatus);
    }
    daoHandler.getTaskHandler().updateAll(tasks);
    //
    st.setProject(null);
    handler.delete(st);
  }

  @Override
  public StatusDto updateStatus(long statusId, String statusName) throws EntityNotFoundException,
                                                                  NotAllowedOperationOnEntityException {
    StatusHandler handler = daoHandler.getStatusHandler();
    StatusDto status = statusToDTO(handler.find(statusId));
    if (status == null) {
      throw new EntityNotFoundException(statusId, Status.class);
    }
    Status curr = handler.findByName(statusName, status.getProject().getId());
    if (curr != null && !status.equals(curr)) {
      throw new NotAllowedOperationOnEntityException(status.getId(), Status.class, "duplicate status name");
    }

    status.setName(statusName);
    return statusToDTO(daoHandler.getStatusHandler().update(statusToEntity(status)));
  }

  @Override
  public StatusDto updateStatus(StatusDto statusDto) throws EntityNotFoundException,
                                                                  NotAllowedOperationOnEntityException {
    StatusHandler handler = daoHandler.getStatusHandler();
    StatusDto status = statusToDTO(handler.find(statusDto.getId()));
    if (status == null) {
      throw new EntityNotFoundException(statusDto.getId(), Status.class);
    }
    StatusDto curr = statusToDTO(handler.findByName(statusDto.getName(), status.getProject().getId()));
    if (curr != null && !status.equals(curr)) {
      throw new NotAllowedOperationOnEntityException(status.getId(), StatusDto.class, "duplicate status name");
    }
    return statusToDTO(daoHandler.getStatusHandler().update(statusToEntity(statusDto)));
  }

  @Override
  public Status statusToEntity(StatusDto statusDto) {
    if(statusDto==null){
      return null;
    }
    Status status = new Status();
    status.setId(statusDto.getId());
    status.setName(statusDto.getName());
    status.setRank(statusDto.getRank());
    status.setProject(statusDto.getProject());
    return status;
  }

  @Override
  public StatusDto statusToDTO(Status status) {
    if(status==null){
      return null;
    }
    StatusDto statusDto = new StatusDto();
    statusDto.setId(status.getId());
    statusDto.setName(status.getName());
    statusDto.setRank(status.getRank());
    statusDto.setProject(status.getProject());
    return statusDto;
  }

  @Override
  public List<StatusDto> listStatusToDTOs(List<Status> status) {
    return status.stream()
            .map(this::statusToDTO)
            .collect(Collectors.toList());
  }

  @Override
  public List<Status> listStatusToEntitys(List<StatusDto> status) {
    return status.stream()
            .map(this::statusToEntity)
            .collect(Collectors.toList());
  }


  private Status findAltStatus(Status st, Project project) {
    List<Status> allSt = new LinkedList<Status>(daoHandler.getStatusHandler().getStatuses(project.getId()));
    Collections.sort(allSt);

    Status other = null;
    for (int i = 0; i < allSt.size(); i++) {
      if (allSt.get(i).equals(st)) {
        if (i > 0) {
          other = allSt.get(i - 1);
        } else if (i + 1 < allSt.size()) {
          other = allSt.get(i + 1);
        }
        break;
      }
    }
    return other;
  }
}
