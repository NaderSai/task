import TaskDrawer from '../taskDrawer/components/TaskDrawer.vue';
import TaskLabels from './components/TaskDrawerComponents/TaskLabels.vue';
import TaskProjects from './components/TaskDrawerComponents/TaskProject.vue';
import TaskAssignment from './components/TaskDrawerComponents/TaskAssignment.vue';
import TaskCommentEditor from './components/TaskDrawerComponents/TaskCommentEditor.vue';
import TaskDescriptionEditor from './components/TaskDrawerComponents/TaskDescriptionEditor.vue';
import LogDetails from './components/TaskDrawerComponents/LogDetails.vue';
import TaskComments from './components/TaskDrawerComponents/TaskComments.vue';
import TaskPriority from './components/TaskDrawerComponents/TaskPriority.vue';
import TaskStatus from "./components/TaskDrawerComponents/TasksStatus.vue";
import TaskFormDatePickers from "./components/TaskDrawerComponents/TaskFormDatePickers.vue";

const components = {
    'task-drawer': TaskDrawer,
    'task-labels': TaskLabels,
    'task-projects': TaskProjects,
    'task-assignment': TaskAssignment,
    'task-comment-editor': TaskCommentEditor,
    'task-description-editor': TaskDescriptionEditor,
    'log-details': LogDetails,
    'task-comments': TaskComments,
    'task-priority' : TaskPriority,
    'task-status' : TaskStatus,
    'task-form-date-pickers': TaskFormDatePickers
};

for (const key in components) {
    Vue.component(key, components[key]);
}