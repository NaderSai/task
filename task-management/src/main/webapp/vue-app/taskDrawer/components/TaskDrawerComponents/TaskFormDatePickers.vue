<template>
  <div>
    <div class="taskPlanDateCalender d-flex align-center">
      <i class="uiIconStartDate uiIconBlue"></i>
      <date-picker
        ref="taskStartDate"
        v-model="startDate"
        :default-value="false"
        :placeholder="$t('label.startDate')"
        :max-value="maximumStartDate"
        class="flex-grow-1 my-auto"
        @input="emitStartDate(startDate)"/>
    </div>
    <div class="taskDueDateCalender d-flex align-center">
      <i class="uiIconDueDate uiIconBlue"></i>
      <date-picker
        ref="taskDueDate"
        v-model="dueDate"
        :default-value="false"
        :placeholder="$t('label.dueDate')"
        :min-value="minimumEndDate"
        class="flex-grow-1 my-auto"
        @input="emitDueDate(dueDate)">
        <template slot="footer">
          <div class="dateFooter">
            <v-btn-toggle
              class="d-flex justify-space-between"
              tile
              color="primary"
              background-color="primary"
              group>
              <v-btn
                value="left"
                class="my-0"
                small
                @click="addBtnDate()">
                {{ $t('label.today') }}
              </v-btn>

              <v-btn
                value="center"
                class="my-0"
                small
                @click="addBtnDate(1)">
                {{ $t('label.tomorrow') }}
              </v-btn>

              <v-btn
                value="right"
                class="my-0"
                small
                @click="addBtnDate(7)">
                {{ $t('label.nextweek') }}
              </v-btn>

              <v-btn
                value="right"
                class="my-0"
                small
                @click="resetDueDate()">
                {{ $t('label.none') }}
              </v-btn>
            </v-btn-toggle>
          </div>
        </template>
      </date-picker>
    </div>
  </div>
</template>
<script>
  export default {
    props: {
      task: {
        type: Object,
        default:() => {
          return {}
        }
      },
      taskDueDate: {
        type: String,
        default: () => 'endDate',
      },
      datePickerTop: {
        type: Boolean,
        default: false,
      },
    },
    data () {
      return {
        startDate: null,
        dueDate: null,
        actualDueDate: {},
        actualTask: {},
        dateShortCutItems: [
          {key:'today',value:this.$t('label.today')},
          {key:'tomorrow',value:this.$t('label.tomorrow')},
          {key:'nextweek',value:this.$t('label.nextweek')},
          {key:'datePicker',value:this.$t('label.pickDate')}
        ],
        dateItem: ''
      }
    },
    computed: {
      minimumEndDate() {
        if (!this.startDate) {
          return null;
        }
        return new Date(this.startDate);
      },
      maximumStartDate() {
        if (!this.dueDate) {
          return null;
        }
        return new Date(this.dueDate);
      },
    },
    watch: {
      task(newVal, oldVal) {
        if(JSON.stringify(newVal) !== '{}') {
          this.actualTask = this.task;
          this.reset();
        }
      },
    },
    mounted() {
      this.actualTask = this.task;
      this.reset();
      document.addEventListener('closeDates',()=> {
        this.$refs.taskStartDate.menu = false;
        this.$refs.taskDueDate.menu = false;
      });

      $('.taskAssignItem').off('click').on('click', (event) => {
        this.$refs.taskStartDate.menu = false;
        this.$refs.taskDueDate.menu = false;
      });
    },
    methods: {
      reset() {
        if (this.actualTask.id!=null) {
          this.startDate = null;
          this.dueDate = null;
          if(this.actualTask.startDate!=null) {
            this.$nextTick().then(() => {
              this.startDate = this.toDate(this.actualTask.startDate.time);
            });
          }
          if(this.actualTask.dueDate!=null) {
            this.$nextTick().then(() => {
              this.dueDate = this.toDate(this.actualTask.dueDate.time);
            });
          }
        } else {
          this.startDate = null;
          this.dueDate = null;
        }
      },
      toDate(date) {
        if (!date) {
          return null;
        } else if (typeof date === 'number') {
          return new Date(date);
        } else if (typeof date === 'string') {
          if (date.indexOf('T') === 10 && date.length > 19) {
            date = date.substring(0, 19);
          }
          return new Date(date);
        } else if (typeof date === 'object') {
          return new Date(date);
        }
      },
      toDateObject(date) {
        date = this.toDate(date);
        if (!date) {
          return null;
        } else {
          const newDateObject ={
            time: '',
            nanos: '',
            year: '',
            month: '',
            day: '',
            hours: '',
            minutes: '',
            seconds: '',
            timezoneOffset: '',
            date: ''
          }
          newDateObject.time = date.getTime();
          newDateObject.year = date.getUTCFullYear() - 1900;
          newDateObject.month = date.getMonth();
          newDateObject.day = date.getDay();
          newDateObject.hours = date.getHours();
          newDateObject.minutes = date.getMinutes();
          newDateObject.seconds = date.getSeconds();
          newDateObject.timezoneOffset = date.getTimezoneOffset();
          newDateObject.date = date.getDate();
          return newDateObject;
        }
      },
      addBtnDate(days) {
        if(days) {
          const date = new Date();
          date.setDate(date.getDate() + days);
          this.dueDate = date;
        } else {
          this.dueDate = new Date();
        }
        this.$refs.taskDueDate.menu = false;
      },
      resetDueDate() {
        this.dueDate = null;
        this.$emit('dueDateChanged','none');
        this.$refs.taskDueDate.menu = false;
      },
      emitStartDate(date) {
        if((!date && this.actualTask.startDate) || (date && !this.actualTask.startDate) || (this.actualTask.startDate && date !== this.actualTask.startDate.time)) {
          this.$emit('startDateChanged',this.toDateObject(date));
        }
      },
      emitDueDate(date) {
        if((!date && this.actualTask.dueDate) || (date && !this.actualTask.dueDate) || (this.actualTask.dueDate && date !== this.actualTask.dueDate.time)) {
          this.$emit('dueDateChanged',this.toDateObject(date));
        }
      },
    }
  }
</script>
