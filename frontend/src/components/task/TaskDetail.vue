<script setup>
import { computed, ref, watch } from "vue";
import { toFormatDate } from "../../lib/utill";

import { FileIcon, CloseIcon, DeleteIcon, EditTaskIcon } from "../icon";
import ImageViewer from "../ImageViewer.vue";
import { useStatusStore } from "../../stores/statuses.js";
import { useRouter } from "vue-router";
import {
  validateSizeInput,
  previewBinary,
  removeURL,
} from "../../lib/utill.js";
import { useUserStore } from "../../stores/user.js";
const router = useRouter();
defineEmits(["userAction", "addEdit"]);
const props = defineProps({
  task: {
    type: Object,
    required: true,
  },
  isEdit: {
    type: Boolean,
    default: false,
  },
  showLoading: {
    type: Boolean,
  },
  allTaskLimit: {
    type: Array,
  },
  fileUrl: {
    type: Array,
  },
  // fileURL2: {
  //   type: Object,
  // },
});
const statusStore = useStatusStore();
const duplicateTask = ref({});
const editMode = ref(props.isEdit);
const isLimit = computed(() => statusStore.isLimit);
const allTaskLimit = ref(props.allTaskLimit);
const maximumTask = computed(() => statusStore.maximumTask);
const noOftask = computed(() => statusStore.noOftask);
const userStore = useUserStore();
const isEditPage = ref(true);
const fileChange = ref(false);

const fileURL = ref(props.fileUrl);

watch(
  () => props.task,
  (newTask) => {
    allTaskLimit.value = props.allTaskLimit;
    for (const key in newTask) {
      if (newTask[key] === null) {
        newTask[key] = "";
      }
    }
    duplicateTask.value = { ...newTask };
    duplicateTask.value.status = { ...newTask.status };
    isEditPage.value = editMode.value && duplicateTask.value.id !== undefined;
  },
  { immediate: true }
);

const selectedImage = ref(null);
const showImageModal = ref(false);
const imageType = ref("pdf");
function openImageModal(file, fileType) {
  if (getFileType(fileType).match(/(pdf|txt)/g)) {
    imageType.value = "embed";
    showImageModal.value = true;
    selectedImage.value = file;
  } else if (getFileType(fileType).match(/(png|jpeg|jpg|gif|bmp|svg)/g)) {
    imageType.value = "image";
    showImageModal.value = true;
    selectedImage.value = file;
  } else {
    showImageModal.value = false;
  }
}

function getFileType(fileName) {
  return fileName.split(".").pop();
}
const isTaskChanged = computed(() => {
  return JSON.stringify(props.task) === JSON.stringify(duplicateTask.value);
});

const createDate = computed(() =>
  toFormatDate(new Date(props.task.createdOn).toLocaleString("en-GB"))
);
const updateDate = computed(() =>
  toFormatDate(new Date(props.task.updatedOn).toLocaleString("en-GB"))
);

const countTitle = computed(() => {
  return duplicateTask.value.title?.trim()?.length;
});
const countDescription = computed(() => {
  return duplicateTask.value.description?.trim()?.length;
});
const countAssignees = computed(() => {
  return duplicateTask.value.assignees?.trim()?.length;
});

const validate = ref({ title: {}, description: {}, assignees: {} });
const previewImagesURL = ref([]);
const fileDetete = ref([]);

const fileCanPreview = (fileName) => {
  if (/\.(png|jpeg|jpg|gif|bmp|svg)$/g.test(fileName)) {
    return "img";
  } else if (/\.(txt|pdf)$/g.test(fileName)) {
    return "embed";
  } else {
    return "any";
  }
};

console.log(previewImagesURL.value.length === 0);

const invalidFile = ref({
  maxSize: {
    msg: "Each file cannot be larger than 20 MB. The following files are not added",
    filename: [],
  },
  maxFile: {
    msg: "Each task can have at most 10 files. The following files are not added",
    filename: [],
  },
  dupFile: {
    msg: "File with the same filename cannot be added or updated to the attachments. Please delete the attachment and add again to update the file",
    filename: [],
  },
});

const disabledInput = ref(false);
const preview = (event) => {
  event.target.files.length >= 10
    ? (disabledInput.value = true)
    : (disabledInput.value = false);
  invalidFile.value.maxSize.filename = [];
  invalidFile.value.maxFile.filename = [];
  invalidFile.value.dupFile.filename = [];
  let countElement = 1;
  [...event.target.files].forEach((element, index) => {
    if (element.size > 20 * 1024 * 1024) {
      invalidFile.value?.maxSize.filename.push(element.name);
      return;
    } else if (
      fileURL.value.length >= 10 ||
      countElement + fileURL.value.length > 10
    ) {
      invalidFile.value?.maxFile.filename.push(element.name);
      return;
    } else if (
      fileURL.value.filter((e) => e.name === element.name).length >= 1 ||
      previewImagesURL.value.filter((e) => e.name === element.name).length >= 1
    ) {
      invalidFile.value?.dupFile.filename.push(element.name);
      return;
    } else {
      previewImagesURL.value.push({
        name: element.name,
        url: element,
      });
      countElement++;
    }
  });
};

const disabledSave = computed(() => {
  const arrStyle = validateSizeInput(
    { propName: "Title", propLenght: countTitle.value, size: 100 },
    { propName: "Description", propLenght: countDescription.value, size: 500 },
    { propName: "Assignees", propLenght: countAssignees.value, size: 30 }
  );

  validate.value.title = arrStyle[0];
  validate.value.description = arrStyle[1];
  validate.value.assignees = arrStyle[2];
  if (
    isTaskChanged.value &&
    previewImagesURL.value.length === 0 &&
    fileChange.value === false
  ) {
    // console.log("isTaskChanged.value && previewImagesURL.value.length === 0");
    return true;
  } else if (
    invalidFile.value.maxSize.filename > 0 ||
    invalidFile.value.maxFile.filename > 0 ||
    invalidFile.value.dupFile.filename > 0
  ) {
    // console.log("previewImagesURL.value.some(e=>e.invalid)");
    return true;
  } else if (fileChange.value) {
    // console.log("fileChange ===false ");
    return false;
  } else if (duplicateTask.value.title === null || countTitle.value <= 0) {
    // console.log("2");
    return true;
  } else if (
    validate.value.title.boolean ||
    validate.value.description.boolean ||
    validate.value.assignees.boolean
  ) {
    // console.log("3");
    return true;
  } else if (limitThisTask.value) {
    // console.log("4");
    return true;
  } else {
    // console.log("else");
    //save กดได้
    return false;
  }
});
const fileInput = ref(null);
function deleteFile(imgUrlObject, index, type, fileName) {
  if (type === "fileDelete") {
    fileChange.value = true;
    fileURL.value.splice(index, 1);
    fileDetete.value.push(fileName);
    removeURL(imgUrlObject);
  } else {
    previewImagesURL.value.splice(index, 1);
    removeURL(imgUrlObject);
  }

  if (fileInput.value) {
    fileInput.value.value = null; // Clear file input
  }
}

function textShow(text) {
  if (text === null) {
    return "italic text-gray-600";
  }
}
function edit(taskId) {
  if (boardStore.isCanEdit) {
    editMode.value = true;
    router.push({ name: "EditTask", params: { taskId: taskId } });
  }
}

const limitThisTask = computed(() => {
  if (isLimit.value) {
    if (
      duplicateTask.value.status.name !== "No Status" &&
      duplicateTask.value.status.name !== "Done"
    ) {
      //ไม่แสดงข้อความเตือนถ้า เลทิก สเตตัสเดิม limit
      if (
        duplicateTask.value.status.name === props.task.status.name &&
        noOftask.value[duplicateTask.value.status.name] === maximumTask.value
      ) {
        return false;
      }
      // >= เพราะ ถ้ามีอยู่ พอดีที่ limit ไว้ แล้ว มีการ add edit แล้วเท่ากับ ค่า limit แล้วจะต้องไม่สามารถทำได้
      else if (
        noOftask.value[duplicateTask.value.status.name] >= maximumTask.value
      ) {
        return true;
      } else {
        return false;
        // return allTaskLimit.value.some((e) => e?.name === duplicateTask.value.status.name);
      }
    } else {
      return false;
    }
  }
});
</script>

<template>
  <div
    class="absolute left-0 right-0 m-auto top-0 bg-black h-screen w-screen bg-opacity-50 z-50"
  >
    <div class="itbkk-modal-task flex h-full items-center justify-center">
      <div
        class="flex flex-col justify-start bg-white h-[80%] shadow-md overflow-y-auto overflow-x-hidden"
        :class="
          !editMode || isEditPage
            ? 'w-[70rem] rounded-s-md'
            : ' rounded-s-md rounded-e-md w-[81rem]'
        "
      >
        <!-- Close Button Container with sticky positioning -->
        <div
          class="w-full flex justify-end sticky top-0 z-10 mb-2"
          :class="!editMode || isEditPage ? 'hidden' : ''"
        >
          <div
            class="cursor-pointer text-error text-2xl pt-5 pr-5"
            @click="$emit('userAction', false)"
          >
            <CloseIcon />
          </div>
        </div>
        <div class="pl-10 pr-5 mt-10">
          <div class="flex flex-row gap-5 items-end w-full rounded-b-lg">
            <textarea
              :readonly="!editMode"
              type="text"
              name="title"
              id="title"
              :class="
                editMode
                  ? countTitle <= 0
                    ? 'border border-red-400 rounded-md p-1 focus:outline-red-500'
                    : validate.title.style
                  : 'border-b'
              "
              class="itbkk-title text-xl rounded-md p-1 font-semibold break-all h-10 w-[90%] border border-gray-800 resize-none read-only:focus:outline-none placeholder:font-normal placeholder:italic"
              @dblclick="edit(task.id)"
              v-model="duplicateTask.title"
              placeholder="Insert Title Here"
            >
            {{ duplicateTask.title }}
            </textarea>
            <div
              :class="
                boardStore.isCanEdit
                  ? ''
                  : 'tooltip tooltip-bottom tooltip-hover '
              "
              data-tip="You need to be board owner to perform this action."
            >
              <!-- <div
                v-show="$route.path !== '/task/add'"
                class="text-2xl hover:bg-gray-200 rounded-full"
                :class="
                  userStore.isCanEdit
                    ? 'cursor-pointer'
                    : 'cursor-not-allowed disabled'
                "
                @click="edit(task.id)"
              >
                <EditTaskIcon />
              </div> -->
            </div>
            <div class="flex flex-col justify-center items-center">
              <p class="font-bold text-sm">Limit</p>
              <p
                class="font-bold text-sm"
                :class="isLimit ? 'text-green-600	' : 'text-rose-600'"
              >
                {{ isLimit ? "On" : "Off" }}
              </p>
            </div>
          </div>
          <div
            class="pl-9 pr-5 flex flex-col justify-end items-end w-[90%]"
            v-if="editMode"
          >
            <span class="text-xs border-0" :class="validate.title.style"
              >{{ countTitle }}/100</span
            >
            <span class="text-xs text-red-500" v-if="validate.title.boolean">
              {{ validate.title.msg }}</span
            >
          </div>
        </div>

        <div class="pl-10 pr-5 mt-3 flex gap-5">
          <div class="flex flex-col w-[50%]">
            <div class="font-bold text-sm">Assignees</div>
            <textarea
              class="itbkk-assignees read-only:focus:outline-none placeholder:text-gray-500 placeholder:italic break-all mt-2 p-2 h-[3rem] w-full rounded-lg border border-gray-800 resize-none text-black"
              :class="
                (editMode ? validate.assignees.style : 'border-b',
                textShow(duplicateTask.assignees))
              "
              :readonly="!editMode"
              @dblclick="edit(task.id)"
              type="text"
              name="assignees"
              id="assignees"
              v-model="duplicateTask.assignees"
              placeholder="Unassigned"
            >
                {{
                duplicateTask.assignees === null ||
                duplicateTask.assignees?.length === 0
                  ? "Unassigned"
                  : duplicateTask.assignees
              }}
                </textarea
            >
            <div
              class="flex flex-col justify-end items-end w-[100%] border-0"
              v-if="editMode"
            >
              <span class="text-xs border-0" :class="validate.assignees.style"
                >{{ countAssignees }}/30</span
              >
              <span
                class="text-xs text-red-500"
                v-if="validate.assignees.boolean"
              >
                {{ validate.assignees.msg }}</span
              >
            </div>
          </div>

          <div class="flex flex-col w-[30%]">
            <label for="category" class="font-bold text-sm">Status</label>
            <select
              id="category"
              :disabled="!editMode"
              v-model="duplicateTask.status.name"
              class="itbkk-status mt-2 bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-primary-500 focus:border-primary-500 block h-[3rem] w-full p-2.5"
            >
              <option
                v-for="status in statusStore.allStatus"
                :selected="
                  duplicateTask.status.name === `${status.name}` ||
                  duplicateTask.status.name === null
                "
                :value="`${status.name}`"
              >
                {{ status.name }}
              </option>
            </select>
          </div>
        </div>
        <div class="text-rose-600 mt-1" v-if="limitThisTask && editMode">
          The status {{ duplicateTask.status.name }} will have too many tasks.
          Please make progress and update status of existing tasks first.
        </div>

        <div class="pl-10 pr-5 mt-3 flex flex-col h-full">
          <div class="font-bold text-sm">Description</div>
          <textarea
            class="itbkk-description read-only:focus:outline-none placeholder:text-gray-500 placeholder:italic break-all mt-2 p-2 rounded-lg border border-gray-800 h-[100%] resize-none text-black"
            :class="
              (editMode ? validate.description.style : 'border-b',
              textShow(duplicateTask.description))
            "
            @dblclick="edit(task.id)"
            :readonly="!editMode"
            type="text"
            name="description"
            id="description"
            v-model="duplicateTask.description"
            placeholder="No Description Provided"
          >
                {{
              duplicateTask.description === null ||
              duplicateTask.description?.length === 0
                ? "No Description Provided"
                : duplicateTask.description
            }}
              </textarea
          >
          <div
            class="flex flex-col justify-end items-end w-[100%] border-0"
            v-if="editMode"
          >
            <span class="text-xs border-0" :class="validate.description.style"
              >{{ countDescription }}/500</span
            >
            <span
              class="text-x text-red-500"
              v-if="validate.description.boolean"
            >
              {{ validate.description.msg }}</span
            >
          </div>
        </div>
        <div
          class="pl-10 pr-5 mt-3 flex flex-col h-full"
          :class="isEditPage ? '' : 'hidden'"
        >
          <label class="font-bold text-sm" for="file_input">Upload file</label>
          <input
            type="file"
            id="file_input"
            ref="fileInput"
            class="mt-2 file-input file-input-bordered file-input-sm w-full max-w-xs"
            :class="
              disabledInput ||
              fileURL.length >= 10 ||
              fileURL.length + previewImagesURL.length >= 10
                ? 'bg-gray-500 cursor-not-allowed'
                : ''
            "
            :disabled="
              disabledInput ||
              fileURL.length >= 10 ||
              fileURL.length + previewImagesURL.length >= 10
            "
            @change="preview"
            multiple
          />
          <div>
            <div
              class="text-red-500 text-sm mt-1 font-medium"
              v-if="invalidFile?.maxSize?.filename?.length > 0"
            >
              {{ invalidFile?.maxSize?.msg }} :
              <span
                class="font-normal"
                v-for="(name, index) in invalidFile?.maxSize?.filename"
                :key="index"
                >{{ name }}
              </span>
            </div>
            <div
              class="text-red-500 text-sm mt-1 font-medium"
              v-if="invalidFile?.maxFile?.filename?.length > 0"
            >
              {{ invalidFile?.maxFile?.msg }} :
              <span
                class="font-normal"
                v-for="(name, index) in invalidFile?.maxFile?.filename"
                :key="index"
                >{{ name }}
              </span>
            </div>
            <div
              class="text-red-500 mt-1 text-sm font-medium"
              v-if="invalidFile?.dupFile?.filename?.length > 0"
            >
              {{ invalidFile?.dupFile?.msg }} :
              <span
                class="font-normal"
                v-for="(name, index) in invalidFile?.dupFile?.filename"
                :key="index"
                >{{ name }}
              </span>
            </div>
          </div>
          <!-- Preview Images Section -->
          <div class="flex flex-wrap gap-4 mt-4">
            <div
              v-for="(file, index) in [...previewImagesURL]"
              :key="index"
              class="relative flex flex-col border border-gray-200 p-2 w-[8rem] h-[6rem] justify-between"
              @click="openImageModal(previewBinary(file.url), file.name)"
            >
              <!-- Delete Button Positioned at Top Right -->
              <div
                class="absolute top-1 right-1 cursor-pointer text-red-500 text-sm"
                @click="deleteFile(file.url, index, 'selectFile')"
              >
                <CloseIcon />
              </div>
              <div class="flex flex-col items-center">
                <img
                  v-if="fileCanPreview(file.name) === 'img'"
                  :src="previewBinary(file.url)"
                  alt="previewImagesURL"
                  class="h-10 w-10 mt-1"
                />
                <embed
                  v-else-if="fileCanPreview(file.name) === 'embed'"
                  :src="previewBinary(file.url)"
                  alt="previewImagesURL"
                  class="h-10 w-10 mt-1"
                />
                <a
                  v-else
                  :href="file.url"
                  target="_blank"
                  class="text-blue-500 underline"
                  ><FileIcon class="h-10 w-10 fill-gray-800 mt-1"
                /></a>

                <!-- Truncated File Name -->

                <p class="text-xs text-center truncate w-full mt-1">
                  {{ file.name }}
                </p>
              </div>
            </div>
          </div>
        </div>

        <div class="flex flex-row justify-end gap-3 pl-10 pr-5 my-5">
          <div class="flex flex-col" v-show="!editMode">
            <div class="font-bold text-sm">Timezone</div>
            <div class="itbkk-timezone mt-2">
              {{ Intl.DateTimeFormat().resolvedOptions().timeZone }}
            </div>
          </div>
          <div class="flex flex-col" v-show="!editMode">
            <div class="font-bold text-sm">Created On</div>
            <div class="itbkk-created-on mt-2">
              {{ createDate }}
            </div>
          </div>
          <div class="flex flex-col" v-show="!editMode">
            <div class="font-bold text-sm">Updated On</div>
            <div class="itbkk-updated-on mt-2">
              {{ updateDate }}
            </div>
          </div>
        </div>

        <div
          class="w-full flex justify-end mb-3"
          :class="editMode ? 'block ' : 'hidden'"
        >
          <div class="flex mr-5 gap-2">
            <button
              :disabled="disabledSave"
              class="itbkk-button-confirm text-white inline-flex items-center focus:ring-4 focus:outline-none font-medium rounded-lg text-sm px-5 py-2.5 text-center bg-gray-300"
              :class="
                disabledSave
                  ? 'cursor-not-allowed disabled'
                  : 'cursor-pointer bg-green-700 hover:bg-green-800 focus:ring-green-300'
              "
              @click="
                $emit('userAction', false),
                  $emit('addEdit', duplicateTask, previewImagesURL, fileDetete)
              "
            >
              SAVE
            </button>
            <button
              class="itbkk-button-cancel text-white inline-flex items-center bg-red-700 hover:bg-red-800 focus:ring-4 focus:outline-none focus:ring-red-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center"
              @click="$emit('userAction', false)"
            >
              CANCEL
            </button>
          </div>
        </div>
      </div>

      <div
        class="itbkk-modal-task bg-gray-100 rounded-e-md h-[80%] w-[11rem] shadow-md overflow-y-auto border-l"
        :class="!editMode || isEditPage ? '' : 'hidden'"
      >
        <!-- Close Button Container with sticky positioning -->
        <div class="w-full flex justify-end sticky top-0 z-10 mb-2">
          <div
            class="cursor-pointer text-error text-2xl pt-5 pr-5"
            @click="$emit('userAction', false)"
          >
            <CloseIcon />
          </div>
        </div>
        <div class="font-bold text-sm pl-2 mt-3">Attachments</div>
        <!-- Scrollable Content Area -->
        <div class=" ">
          <div
            v-for="(file, index) in fileURL"
            :key="index"
            v-show="fileURL"
            class="flex flex-col items-center justify-between w-full border-b border-gray-200 p-2"
            
          >
            <div class="flex flex-col items-center justify-between w-full "
            @click="openImageModal(file.url, file.name)">
              <img
                v-if="fileCanPreview(file.name) === 'img'"
                :src="file.url"
                alt="previewImagesURL"
                class="h-10 w-10 mt-1"
              />
              <embed
                v-else-if="fileCanPreview(file.name) === 'embed'"
                :src="file.url"
                alt="previewImagesURL"
                class="h-10 w-10 mt-1"
              />
              <a v-else :href="file.url" :download="file.name" target="_blank"
                ><FileIcon class="h-10 w-10 fill-gray-800" />
              </a>
              <p class="text-xs text-center truncate w-full mt-1">
                {{ file.name }}
              </p>
            </div>
            <div
              :class="isEditPage ? 'block' : 'hidden'"
              @click="deleteFile(file.url, index, 'fileDelete', file.name)"
              class="bottom-1 right-1 cursor-pointer fill-rose-300 text-sm"
            >
              <DeleteIcon class="h-7 w-7" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <ImageViewer
    :imageSrc="selectedImage"
    :visible="showImageModal"
    :imageType="imageType"
    @close="showImageModal = false"
  />
</template>

<style scoped>
@media not all and (min-height: 830px) {
  .max-h-800px {
    height: 65%;
  }
}
</style>
