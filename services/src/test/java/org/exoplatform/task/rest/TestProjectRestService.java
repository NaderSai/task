package org.exoplatform.task.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import java.util.*;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.task.rest.model.PaginatedTaskList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.task.TestUtils;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.dto.ProjectDto;
import org.exoplatform.task.dto.StatusDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.legacy.service.UserService;
import org.exoplatform.task.model.User;
import org.exoplatform.task.service.*;
import org.exoplatform.task.storage.ProjectStorage;
import org.exoplatform.task.storage.StatusStorage;

@RunWith(MockitoJUnitRunner.class)
public class TestProjectRestService {
  @Mock
  TaskService    taskService;

  @Mock
  ProjectService projectService;

  @Mock
  ProjectStorage projectStorage;

  @Mock
  StatusService  statusService;

  @Mock
  StatusStorage  statusStorage;

  @Mock
  UserService    userService;

  @Mock
  SpaceService   spaceService;

  @Mock
  CommentService commentService;

  @Mock
  LabelService   labelService;

  @Mock
  IdentityManager identityManager;

  @Before
  public void setup() {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
  }

  @Test
  public void testGetTasks() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService,
                                                          commentService,
                                                          projectService,
                                                          statusService,
                                                          userService,
                                                          spaceService,
                                                          labelService);
    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));
    TaskDto task1 = new TaskDto();
    TaskDto task2 = new TaskDto();
    TaskDto task3 = new TaskDto();
    TaskDto task4 = new TaskDto();
    List<TaskDto> uncompletedTasks = new ArrayList<TaskDto>();
    task1.setCompleted(true);
    uncompletedTasks.add(task2);
    uncompletedTasks.add(task3);
    uncompletedTasks.add(task4);
    List<TaskDto> overdueTasks = new ArrayList<TaskDto>();
    overdueTasks.add(task1);
    overdueTasks.add(task2);
    List<TaskDto> incomingTasks = new ArrayList<TaskDto>();
    incomingTasks.add(task1);
    incomingTasks.add(task2);

    when(taskService.getUncompletedTasks("root", 20)).thenReturn(uncompletedTasks);
    when(taskService.countUncompletedTasks("root")).thenReturn(Long.valueOf(uncompletedTasks.size()));
    when(taskService.getOverdueTasks("root", 20)).thenReturn(overdueTasks);
    when(taskService.countOverdueTasks("root")).thenReturn(Long.valueOf(overdueTasks.size()));
    when(taskService.getIncomingTasks("root", 0, 20)).thenReturn(incomingTasks);
    when(taskService.countIncomingTasks("root")).thenReturn(incomingTasks.size());
    when(taskService.findTasks(eq("root"), eq("searchTerm"), anyInt())).thenReturn(Collections.singletonList(task4));
    when(taskService.countTasks(eq("root"), eq("searchTerm"))).thenReturn(1L);

    // When
    Response response = taskRestService.getTasks("overdue", null, 0, 20, false, false);
    Response response1 = taskRestService.getTasks("incoming", null, 0, 20, false, false);
    Response response2 = taskRestService.getTasks("", null, 0, 20, false, false);
    Response response3 = taskRestService.getTasks("whatever", "searchTerm", 0, 20, true, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    PaginatedTaskList tasks = (PaginatedTaskList) response.getEntity();
    assertNotNull(tasks);
    assertEquals(2, tasks.getTasksNumber());

    assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
    PaginatedTaskList tasks1 = (PaginatedTaskList) response1.getEntity();
    assertNotNull(tasks1);
    assertEquals(2, tasks1.getTasksNumber());

    assertEquals(Response.Status.OK.getStatusCode(), response2.getStatus());
    PaginatedTaskList tasks2 = (PaginatedTaskList) response2.getEntity();
    assertNotNull(tasks2);
    assertEquals(3, tasks2.getTasksNumber());

    assertEquals(Response.Status.OK.getStatusCode(), response3.getStatus());
    // JSONObject tasks3JsonObject = (JSONObject) response3.getEntity();
    // assertNotNull(tasks3JsonObject);
    // assertTrue(tasks3JsonObject.has("size"));
    // assertTrue(tasks3JsonObject.has("tasks"));
    // JSONArray tasks3 = (JSONArray) tasks3JsonObject.get("tasks");
    // assertNotNull(tasks3);
    // assertEquals(1, tasks3.length());
    // Long tasks3Size = (Long) tasks3JsonObject.get("size");
    // assertEquals(1L, tasks3Size.longValue());
  }

  @Test
  public void testGetProjects() throws Exception {
    // Given
    ProjectRestService projectRestService = new ProjectRestService(taskService,
                                                                   commentService,
                                                                   projectService,
                                                                   statusService,
                                                                   userService,
                                                                   spaceService,
                                                                   labelService,
                                                                   identityManager);
    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));

    ProjectDto project1 = new ProjectDto();
    project1.setName("project1");
    projectService.createProject(project1);
    ProjectDto project2 = new ProjectDto();
    project2.setName("project2");
    projectService.createProject(project2);
    ProjectDto project3 = new ProjectDto();
    project3.setName("project3");
    projectService.createProject(project3);

    // When
    Response response = projectRestService.getProjects(null,"", "ALL", -1, -1,false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    assertNotNull(response.getEntity());
  }

  @Test
  public void testGetDefaultStatusByProjectId() throws Exception {
    // Given
    ProjectRestService projectRestService = new ProjectRestService(taskService,
                                                                   commentService,
                                                                   projectService,
                                                                   statusService,
                                                                   userService,
                                                                   spaceService,
                                                                   labelService,
                                                                   identityManager);
    Identity john = new Identity("john");
    ConversationState.setCurrent(new ConversationState(john));
    ProjectDto project = new ProjectDto();
    project.setId(1);
    Set<String> manager = new HashSet<String>();
    manager.add("john");
    project.setManager(manager);
    StatusDto status = new StatusDto();
    status.setId(Long.valueOf(1));
    status.setName("status 1");

    when(projectService.getProject(1L)).thenReturn(project);
    when(statusService.getDefaultStatus(1L)).thenReturn(status);
    when(projectService.getManager(1)).thenReturn(manager);
    // When
    Response response = projectRestService.getDefaultStatusByProjectId(3);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

  }

  @Test
  public void testGetProjectById() throws Exception {
    // Given
    ProjectRestService projectRestService = new ProjectRestService(taskService,
                                                                   commentService,
                                                                   projectService,
                                                                   statusService,
                                                                   userService,
                                                                   spaceService,
                                                                   labelService,
                                                                   identityManager);
    Identity john = new Identity("john");
    ConversationState.setCurrent(new ConversationState(john));
    ProjectDto project = new ProjectDto();
    project.setId(1);
    Set<String> manager = new HashSet<String>();
    manager.add("john");
    project.setManager(manager);

    when(projectService.getProject(1L)).thenReturn(project);

    // When
    Response response = projectRestService.getProjectById(1,true);
    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    assertNotNull(response.getEntity());

  }

  @Test
  public void testAddProject() throws Exception {
    // Given
    ProjectRestService projectRestService = new ProjectRestService(taskService,
                                                                   commentService,
                                                                   projectService,
                                                                   statusService,
                                                                   userService,
                                                                   spaceService,
                                                                   labelService,
                                                                   identityManager);
    Identity john = new Identity("john");
    ConversationState.setCurrent(new ConversationState(john));
    Set<String> manager = new HashSet<String>();
    manager.add("john");
    ProjectDto projectDto = new ProjectDto();
    projectDto.setId(1);
    projectDto.setName("john");
    projectDto.setDescription("bla bla bla");
    projectDto.setManager(manager);
    projectService.createProject(projectDto);

    when(projectService.createProject(any())).thenReturn(projectDto);

    Response response = projectRestService.createProject(projectDto);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    projectDto.setName(null);
    projectService.createProject(projectDto);

    when(projectService.createProject(any())).thenReturn(projectDto);

    response = projectRestService.createProject(projectDto);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

  }

  @Test
  public void testUpdateProject() throws Exception {
    // Given
    ProjectRestService projectRestService = new ProjectRestService(taskService,
                                                                   commentService,
                                                                   projectService,
                                                                   statusService,
                                                                   userService,
                                                                   spaceService,
                                                                   labelService,
                                                                   identityManager);
    Identity john = new Identity("john");
    Identity exo = new Identity("exo");
    ConversationState.setCurrent(new ConversationState(john));
    Set<String> manager = new HashSet<String>();
    manager.add("john");
    Set<String> participator = new HashSet<String>();
    participator.add("exo");
    ProjectDto projectDto = new ProjectDto();
    projectDto.setId(1);
    projectDto.setDescription("bla bla bla");
    projectDto.setManager(manager);
    projectDto.setParticipator(participator);
    projectService.updateProject(projectDto);

    when(projectService.updateProject(any())).thenReturn(projectDto);

    Response response = projectRestService.updateProject(projectDto.getId(), projectDto);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

    projectDto.setName("john");
    projectService.updateProject(projectDto);

    when(projectService.updateProject(any())).thenReturn(projectDto);
    when(projectService.getProject(projectDto.getId())).thenReturn(projectDto);
    response = projectRestService.updateProject(projectDto.getId(), projectDto);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(john));

    when(projectService.updateProject(any())).thenReturn(projectDto);
    when(projectService.getProject(projectDto.getId())).thenReturn(projectDto);
    response = projectRestService.updateProject(projectDto.getId(), projectDto);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

  }

  @Test
  public void testGetSatusesByProjectId() throws Exception {
    // Given
    ProjectRestService projectRestService = new ProjectRestService(taskService,
                                                                   commentService,
                                                                   projectService,
                                                                   statusService,
                                                                   userService,
                                                                   spaceService,
                                                                   labelService,
                                                                   identityManager);
    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));

    ProjectDto project1 = new ProjectDto();
    project1.setName("project1");
    projectService.createProject(project1);
    ProjectDto project2 = new ProjectDto();
    project2.setName("project2");
    projectService.createProject(project2);
    ProjectDto project3 = new ProjectDto();
    project3.setName("project3");
    projectService.createProject(project3);

    StatusDto status1 = new StatusDto(3, "ToDo", 1, projectStorage.projectToEntity(project1));
    StatusDto status2 = new StatusDto(4, "ToDo", 2, projectStorage.projectToEntity(project1));

    List<StatusDto> statuses = new ArrayList<>();
    statuses.add(status1);
    statuses.add(status2);
    List<Status> list = statusStorage.listStatusToEntitys(statuses);
    Set<Status> foo = new HashSet<Status>(list);
    project1.setStatus(foo);

    Set<String> participator = new HashSet<String>();
    participator.add("Tib");
    project1.setParticipator(participator);

    Set<String> managers = new HashSet<String>();
    managers.add("Tib");
    project1.setManager(managers);

    Set<String> participator1 = new HashSet<String>();
    participator1.add("root");
    project2.setParticipator(participator1);

    Set<String> managers1 = new HashSet<String>();
    managers1.add("root");
    project2.setManager(managers1);

    // When
    Response response1 = projectRestService.getStatusesByProjectId(project1.getId());

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response1.getStatus());

    // When
    when(projectService.getProject(project1.getId())).thenReturn(project1);
    Response response2 = projectRestService.getStatusesByProjectId(project1.getId());

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response2.getStatus());

    // When
    when(projectService.getProject(project2.getId())).thenReturn(project2);
    Response response3 = projectRestService.getStatusesByProjectId(project2.getId());

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response3.getStatus());
    assertNotNull(response3.getEntity());

  }

  @Test
  public void testfindUsersToMention() throws Exception {
    // Given
    ProjectRestService projectRestService = new ProjectRestService(taskService,
                                                                   commentService,
                                                                   projectService,
                                                                   statusService,
                                                                   userService,
                                                                   spaceService,
                                                                   labelService,
                                                                   identityManager);

    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));

    ProjectDto project1 = new ProjectDto();
    project1.setName("project1");

    when(projectService.getProject(project1.getId())).thenReturn(project1);

    final User user = TestUtils.getUser();
    ListAccess<User> lists = new ListAccess<User>() {
      @Override
      public User[] load(int i, int i1) throws Exception, IllegalArgumentException {
        return new User[] { user };
      }

      @Override
      public int getSize() throws Exception {
        return 1;
      }
    };

    when(userService.findUserByName("root")).thenReturn(lists);

    Response response = projectRestService.getUsersByQueryAndProjectName("root", "project1");
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void testDeleteProject() throws Exception {
    // Given
    ProjectRestService projectRestService = new ProjectRestService(taskService,
                                                                   commentService,
                                                                   projectService,
                                                                   statusService,
                                                                   userService,
                                                                   spaceService,
                                                                   labelService,
                                                                   identityManager);
    Identity john = new Identity("john");
    ConversationState.setCurrent(new ConversationState(john));
    Set<String> manager = new HashSet<String>();
    manager.add("john");
    ProjectDto projectDto = new ProjectDto();
    projectDto.setId(1);
    projectDto.setName("john");
    projectDto.setDescription("bla bla bla");
    projectDto.setManager(manager);

    when(projectService.getProject(projectDto.getId())).thenReturn(projectDto);

    Response response1 = projectRestService.deleteProject(projectDto.getId(), false, 0, 0);
    assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
  }

  @Test
  public void testCloneProject() throws Exception {
    // Given
    ProjectRestService projectRestService = new ProjectRestService(taskService,
                                                                   commentService,
                                                                   projectService,
                                                                   statusService,
                                                                   userService,
                                                                   spaceService,
                                                                   labelService,
                                                                   identityManager);
    Identity john = new Identity("john");
    ConversationState.setCurrent(new ConversationState(john));
    Set<String> manager = new HashSet<String>();
    manager.add("john");
    ProjectDto projectDto = new ProjectDto();
    projectDto.setId(1);
    projectDto.setName("john");
    projectDto.setDescription("bla bla bla");
    projectDto.setManager(manager);

    ProjectDto projectCloned = new ProjectDto();
    projectCloned.setId(2);
    projectCloned.setName("john");
    projectCloned.setDescription("bla bla bla");
    projectCloned.setManager(manager);

    when(projectService.cloneProject(projectDto.getId(), true)).thenReturn(projectCloned);

    Response response1 = projectRestService.cloneProject(projectDto);
    assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
  }

  @Test
  public void testChangeProjectColor() throws Exception {
    // Given
    ProjectRestService projectRestService = new ProjectRestService(taskService,
                                                                   commentService,
                                                                   projectService,
                                                                   statusService,
                                                                   userService,
                                                                   spaceService,
                                                                   labelService,
                                                                   identityManager);
    Identity john = new Identity("john");
    ConversationState.setCurrent(new ConversationState(john));
    Set<String> manager = new HashSet<String>();
    manager.add("john");
    ProjectDto projectDto = new ProjectDto();
    projectDto.setId(1);
    projectDto.setColor("");
    projectDto.setDescription("bla bla bla");
    projectDto.setManager(manager);

    ProjectDto projectDto1 = new ProjectDto();
    projectDto.setId(1);
    projectDto.setColor("red");
    projectDto.setDescription("bla bla bla");
    projectDto.setManager(manager);

    when(projectService.getProject(projectDto.getId())).thenReturn(projectDto);
    when(projectService.updateProject(projectDto)).thenReturn(projectDto1);

    Response response1 = projectRestService.changeProjectColor(projectDto.getId(), "red");
    assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
  }

}
