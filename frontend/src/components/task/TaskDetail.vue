<script setup>
import { computed, ref, watch } from "vue";
import { toFormatDate } from "../../lib/utill";

import { CloseIcon } from "../icon";
import ImageViewer from "../ImageViewer.vue";
import { useStatusStore } from "../../stores/statuses.js";
import { useRouter, useRoute } from "vue-router";
import {
  validateSizeInput,
  previewBinary,
  removeURL,
} from "../../lib/utill.js";
import { useBoardStore } from "../../stores/boards.js";
import AttachmentLoadingVue from "../loading/AttachmentLoading.vue";
import ConfirmModal from "../modal/ConfirmModal.vue";
import FilePreViewList from "../FilePreViewList.vue";
import { downloadfile } from "../../lib/fetchUtill.js";
import Toast from "../modal/Toasts.vue";
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
  showAttachment: {
    type: Boolean,
  },
  showLoadingFile: {
    type: Boolean,
  },
  allTaskLimit: {
    type: Array,
  },
  fileUrl: {
    type: Array,
  },
});
const showToast = ref(false);
const typeToast = ref("");
const messageToast = ref("");
const showLoadingImage = ref(false);
const fileURL = ref(props.fileUrl);
const showRedo = ref(false);
const fileSelectRedo = ref([]);
const showRedoButton = ref(false);
const router = useRouter();
const route = useRoute();
const boardId = ref(route.params.boardId);
const taskId = ref(route.params.taskId);
const previewImagesURL = ref([]);
const selectedImage = ref(null);
const showImageModal = ref(false);
const imageType = ref("pdf");
const statusStore = useStatusStore();
const duplicateTask = ref({});
const editMode = ref(props.isEdit);
const isLimit = computed(() => statusStore.isLimit);
const allTaskLimit = ref(props.allTaskLimit);
const maximumTask = computed(() => statusStore.maximumTask);
const noOftask = computed(() => statusStore.noOftask);
const boardStore = useBoardStore();
const isEditPage = ref(true);
const fileChange = ref(false); //เป็น true เมื่อ file ที่มีอยู่แล้วเปลี่ยน
const validate = ref({ title: {}, description: {}, assignees: {} });
const fileDetete = ref({ fileName: [], fileUrl: [] });
const disabledInput = ref(false);
const fileInput = ref(null);
const maxFile = ref(false); // false คือ < 10
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
const isDraggedOver = ref(false);

const handleFileChange = (event) => {
  if (!disabledInput.value) {
    preview(event.target.files);
  }
};

const dropHandler = (event) => {
  if (!disabledInput.value) {
    preview(event.dataTransfer.files);
    isDraggedOver.value = false;
  }
};

const dragEnterHandler = () => {
  if (!disabledInput.value) {
    isDraggedOver.value = true;
  }
};

const dragLeaveHandler = () => {
  if (!disabledInput.value) {
    isDraggedOver.value = false;
  }
};

const fileInputClick = () => {
  fileInput.value.click();
};

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

watch(
  [fileURL.value, previewImagesURL.value],
  ([newfileURL, newpreviewImagesURL]) => {
    if (
      newfileURL.length + newpreviewImagesURL.length >= 10 ||
      newfileURL.length >= 10 ||
      newpreviewImagesURL.length >= 10
    ) {
      //ขนาดfile เกินไม่ให้กด เลือก file เพิ่ม
      maxFile.value = true;
      disabledInput.value = true;
    } else {
      maxFile.value = false;
      disabledInput.value = false;
    }
  },
  { immediate: true }
);

const fileCanPreview = (fileName) => {
  if (/\.(png|jpeg|jpg|gif|bmp|svg)$/g.test(fileName)) {
    return "img";
  } else if (/\.(txt|pdf)$/g.test(fileName)) {
    return "embed";
  } else {
    return "any";
  }
};

async function downloadFile(filename, action) {
  const fileType = getFileType(filename);
  if (
    action === "preview" &&
    fileType.match(/(png|jpeg|jpg|gif|bmp|svg|txt|pdf)/g)
  ) {
    showImageModal.value = true;
  }
  showLoadingImage.value = true;
  const resFile = await downloadfile(boardId.value, taskId.value, filename);
  showLoadingImage.value = false;
  if (typeof resFile !== "number") {
    if (fileType.match(/(png|jpeg|jpg|gif|bmp|svg)/g) && action === "preview") {
      imageType.value = "image";
      selectedImage.value = resFile;
    } else if (fileType.match(/(txt|pdf)/g) && action === "preview") {
      imageType.value = "embed";
      selectedImage.value = resFile;
    } else {
      const a = document.createElement("a");
      a.href = resFile;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      a.remove();
      removeURL(resFile);
      showImageModal.value = false;
    }
  } else {
    showToast.value = true;
    typeToast.value = "danger";
    messageToast.value = "Download file failed";
  }
}

function openImageModal(file, filename, action) {
  if (action !== "choose" && /\.(rtf)$/g.test(filename) === false) {
    downloadFile(filename, action);
  } else {
    if (getFileType(filename).match(/(txt|pdf|rtf)/g)) {
      imageType.value = "embed";
      showImageModal.value = true;
      selectedImage.value = file;
    } else if (getFileType(filename).match(/(png|jpeg|jpg|gif|bmp|svg)/g)) {
      imageType.value = "image";
      showImageModal.value = true;
      selectedImage.value = file;
    } else {
      imageType.value = "otherType";
      showImageModal.value = false;
    }
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

let countElement = 0;
const preview = (files) => {
  files?.length >= 10
    ? (disabledInput.value = true)
    : (disabledInput.value = false);
  invalidFile.value.maxSize.filename = [];
  invalidFile.value.maxFile.filename = [];
  invalidFile.value.dupFile.filename = [];

  [...files].forEach((element, index) => {
    if (element.size > 20 * 1024 * 1024) {
      invalidFile.value?.maxSize.filename.push(element.name);
      return;
    } else if (
      fileURL.value.length >= 10 ||
      countElement + fileURL.value.length >= 10 ||
      maxFile.value
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
      console.log(maxFile.value);

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
    return true;
  } else if (
    invalidFile.value.maxSize.filename > 0 ||
    invalidFile.value.maxFile.filename > 0 ||
    invalidFile.value.dupFile.filename > 0
  ) {
    return true;
  } else if (fileChange.value) {
    return false;
  } else if (duplicateTask.value.title === null || countTitle.value <= 0) {
    return true;
  } else if (
    validate.value.title.boolean ||
    validate.value.description.boolean ||
    validate.value.assignees.boolean
  ) {
    return true;
  } else if (limitThisTask.value) {
    return true;
  } else {
    //save กดได้
    return false;
  }
});
function deleteFile(imgUrlObject, index, type, fileName) {
  invalidFile.value.maxFile.filename = [];
  if (type === "fileDelete") {
    fileChange.value = true;
    fileURL.value.splice(index, 1);
    fileDetete.value.fileName.push(fileName);
    fileDetete.value.fileUrl.push(imgUrlObject);
  } else {
    previewImagesURL.value.splice(index, 1);
    --countElement;
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
        duplicateTask.value.status.name === props.task?.status?.name &&
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

const numberFileCanAdd = computed(() => {
  return (
    10 -
    (fileSelectRedo.value.length +
      fileURL.value.length +
      previewImagesURL.value.length)
  );
});

const numberFileCanRedo = ref(0);
function redoFile(userAction) {
  if (userAction && maxFile.value === false) {
    if (fileSelectRedo.value.length >= 0) {
      let arr = {
        fileName: [...fileDetete.value.fileName],
        fileUrl: [...fileDetete.value.fileUrl],
      };
      fileURL.value.push(...fileSelectRedo.value);

      for (let i = arr.fileName.length - 1; i >= 0; i--) {
        // หาค่า index ที่ตรงกันใน fileSelectRedo
        const index = fileSelectRedo.value.findIndex(
          (e) => e.name === arr.fileName[i]
        );

        if (index !== -1) {
          // ลบข้อมูลที่ตำแหน่ง i
          fileDetete.value.fileName.splice(i, 1);
          fileDetete.value.fileUrl.splice(i, 1);
        }
      }
      showToast.value = true;
      typeToast.value = "success";
      messageToast.value = "Redo file success";
    }
  } else if (userAction && maxFile.value) {
    showToast.value = true;
    typeToast.value = "warning";
    messageToast.value = "Can not Redo file .You have already max 10 file ";
  } else {
    showToast.value = false;
  }
  if (fileDetete.value.fileName.length <= 0) {
    showRedoButton.value = false;
    fileChange.value = false;
  }
  fileSelectRedo.value = [];
  showRedo.value = false;
}
</script>

<template>
  <div
    class="absolute left-0 right-0 m-auto top-0 bg-black h-screen w-screen bg-opacity-50 z-50"
  >
    <div class="itbkk-modal-task flex h-full items-center justify-center">
      <div
        class="flex flex-col justify-start bg-white h-[80%] shadow-md overflow-y-auto overflow-x-hidden"
        :class="
          showAttachment
            ? 'w-[70rem] rounded-s-md'
            : ' rounded-s-md rounded-e-md w-[81rem]'
        "
      >
        <div
          class="w-full flex justify-end sticky top-0 z-10 mb-2"
          :class="showAttachment ? 'hidden' : ''"
        >
          <div
            class="cursor-pointer text-error text-2xl pt-5 pr-5"
            @click="$emit('userAction', false)"
          >
            <CloseIcon />
          </div>
        </div>
        <div class="pl-10 pr-5" :class="showAttachment ? 'mt-10' : ''">
          <div class="font-bold text-base">Title</div>
          <div class="flex flex-row gap-5 items-end w-full rounded-b-lg mt-2">
            <div
              v-if="showLoading"
              class="animate-pulse bg-gray-300 rounded-md h-10 w-[90%]"
            ></div>
            <textarea
              v-else
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
              class="itbkk-title text-xl rounded-md p-1 font-semibold break-all h-10 border-gray-800 w-[90%] border resize-none read-only:focus:outline-none placeholder:font-normal placeholder:italic"
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
            ></div>
            <div class="flex flex-col justify-center items-center">
              <p class="font-bold text-base">Limit</p>
              <p
                class="font-bold text-sm"
                :class="isLimit ? 'text-green-600	' : 'text-rose-600'"
              >
                {{ isLimit ? "On" : "Off" }}
              </p>
            </div>
          </div>
          <div
            class="pl-9 pr-5 flex flex-col justify-end items-end w-[91%]"
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
            <div class="font-bold text-base">Assignees</div>
            <div
              v-if="showLoading"
              class="animate-pulse bg-gray-300 rounded-md h-[3rem] w-full"
            ></div>
            <textarea
              v-else
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
            <label for="category" class="font-bold text-base">Status</label>
            <div
              v-if="showLoading"
              class="animate-pulse bg-gray-300 rounded-md h-[3rem] w-full"
            ></div>
            <div v-else>
              <select
                v-show="editMode"
                id="category"
                :disabled="!editMode"
                v-model="duplicateTask.status.name"
                class="itbkk-status mt-2 border border-gray-300 text-sm rounded-lg focus:ring-primary-500 focus:border-primary-500 block h-[3rem] w-full p-2.5"
                :class="
                  editMode
                    ? 'bg-gray-50 text-gray-900'
                    : 'bg-gray-200 text-gray-900'
                "
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

              <div
                v-show="!editMode"
                class="bg-gray-200 text-gray-900 rounded-lg p-2.5 mt-2"
              >
                <p>{{ duplicateTask.status.name }}</p>
              </div>
            </div>
          </div>
        </div>
        <div
          class="text-end pr-5 text-rose-600 mt-1"
          v-if="limitThisTask && editMode"
        >
          The status {{ duplicateTask.status.name }} will have too many tasks.
          Please make progress and update status of existing tasks first.
        </div>

        <div class="pl-10 pr-5 mt-3 flex flex-col h-full">
          <div class="font-bold text-base">Description</div>
          <div
            v-if="showLoading"
            class="animate-pulse bg-gray-300 rounded-md h-full w-full"
          ></div>
          <textarea
            v-else
            class="itbkk-description read-only:focus:outline-none placeholder:text-gray-500 placeholder:italic break-all mt-2 p-2 rounded-lg border border-gray-800 h-full resize-none text-black"
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
          <label class="font-bold text-base mb-2" for="file_input">File</label>
          <div class="w-full h-full flex flex-col">
            <div
              class="border-dashed border-2 border-gray-400 py-10 flex flex-col justify-center items-center rounded-md"
              @drop.prevent="dropHandler"
              @dragover.prevent
              @dragleave="dragLeaveHandler"
              @dragenter="dragEnterHandler"
              @click="fileInputClick"
              :class="
                disabledInput || showLoadingFile
                  ? 'bg-gray-200 cursor-not-allowed'
                  : ' ' || (isDraggedOver && !showLoadingFile)
                  ? 'bg-gray-100 cursor-pointer hover:bg-gray-200'
                  : ''
              "
            >
              <!-- overlay -->
              <div
                id="overlay"
                class="w-full h-full pointer-events-none z-50 flex flex-col items-center justify-center"
                :class="
                  isDraggedOver && !disabledInput && !showLoadingFile
                    ? 'draggedover'
                    : ''
                "
              >
                <i>
                  <svg
                    class="fill-current w-16 h-16 mb-3 text-blue-700"
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 24 24"
                  >
                    <path
                      d="M19.479 10.092c-.212-3.951-3.473-7.092-7.479-7.092-4.005 0-7.267 3.141-7.479 7.092-2.57.463-4.521 2.706-4.521 5.408 0 3.037 2.463 5.5 5.5 5.5h13c3.037 0 5.5-2.463 5.5-5.5 0-2.702-1.951-4.945-4.521-5.408zm-7.479-1.092l4 4h-3v4h-2v-4h-3l4-4z"
                    />
                  </svg>
                </i>
                <p class="text-lg text-blue-700">Drop files to upload</p>
              </div>

              <svg
                class="fill-current w-16 h-16"
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                v-show="!isDraggedOver || disabledInput || showLoadingFile"
                :class="
                  disabledInput || showLoadingFile
                    ? 'text-gray-400'
                    : 'text-gray-700'
                "
              >
                <path
                  d="M19.479 10.092c-.212-3.951-3.473-7.092-7.479-7.092-4.005 0-7.267 3.141-7.479 7.092-2.57.463-4.521 2.706-4.521 5.408 0 3.037 2.463 5.5 5.5 5.5h13c3.037 0 5.5-2.463 5.5-5.5 0-2.702-1.951-4.945-4.521-5.408zm-7.479-1.092l4 4h-3v4h-2v-4h-3l4-4z"
                />
              </svg>
              <p
                class="font-semibold flex flex-wrap text-base justify-center"
                v-show="!isDraggedOver || disabledInput || showLoadingFile"
                :class="
                  disabledInput || showLoadingFile
                    ? 'text-gray-400'
                    : 'text-gray-600'
                "
              >
                <span>Max file : 10 </span>,<span>
                  Your can add : {{ numberFileCanAdd }} file</span
                >
              </p>
              <p
                class="font-semibold flex flex-wrap text-base justify-center"
                v-show="!isDraggedOver || disabledInput || showLoadingFile"
                :class="
                  disabledInput || showLoadingFile
                    ? 'text-gray-400'
                    : 'text-gray-600'
                "
              >
                <span>Drag and drop your</span>&nbsp;<span
                  >files anywhere or</span
                >
              </p>
              <input
                ref="fileInput"
                type="file"
                multiple
                class="hidden"
                :disabled="disabledInput"
                @change="handleFileChange"
                v-show="!isDraggedOver || disabledInput || showLoadingFile"
              />
              <p
                class="text-gray-400 font-normal text-base flex flex-wrap justify-center"
                v-show="!isDraggedOver || disabledInput || showLoadingFile"
                :class="
                  disabledInput || showLoadingFile
                    ? 'text-gray-400'
                    : 'text-gray-500'
                "
              >
                <span>or click to upload</span>
              </p>
              <div>
                <div
                  class="text-red-500 text-sm mt-1 mx-8 font-medium"
                  v-if="invalidFile?.maxSize?.filename?.length > 0"
                >
                  {{ invalidFile?.maxSize?.msg }} :
                  <span
                    class="font-normal"
                    v-for="(name, index) in invalidFile?.maxSize?.filename"
                    :key="index"
                    >{{ index === 0 ? "" : "," }} {{ name }}
                  </span>
                </div>
                <div
                  class="text-red-500 text-sm mt-1 mx-8 font-medium"
                  v-if="invalidFile?.maxFile?.filename?.length > 0"
                >
                  {{ invalidFile?.maxFile?.msg }} :
                  <span
                    class="font-normal"
                    v-for="(name, index) in invalidFile?.maxFile?.filename"
                    :key="index"
                    >{{ index === 0 ? "" : "," }} {{ name }}</span
                  >
                </div>
                <div
                  class="text-red-500 mt-1 mx-8 text-sm font-medium"
                  v-if="invalidFile?.dupFile?.filename?.length > 0"
                >
                  {{ invalidFile?.dupFile?.msg }} :
                  <span
                    class="font-normal"
                    v-for="(name, index) in invalidFile?.dupFile?.filename"
                    :key="index"
                    >{{ index === 0 ? "" : "," }} {{ name }}
                  </span>
                </div>
              </div>
            </div>

            <h1 class="py-2 font-bold sm:text-base text-gray-900">To Upload</h1>

            <ul class="flex gap-2 flex-wrap">
              <li
                v-if="previewImagesURL?.length === 0"
                class="h-full w-full text-center flex flex-col items-center justify-center"
              >
                <img
                  class="mx-auto w-32"
                  src="https://user-images.githubusercontent.com/507615/54591670-ac0a0180-4a65-11e9-846c-e55ffce0fe7b.png"
                  alt="no data"
                />
                <span class="text-small text-gray-500">No files selected</span>
              </li>

              <li
                v-for="(file, index) in [...previewImagesURL]"
                :key="index"
                class="w-1/2 sm:w-1/3 md:w-1/4 lg:w-1/6 xl:w-1/8 h-24 mr-2"
              >
                <FilePreViewList
                  :filename="file.name"
                  :fileurl="previewBinary(file.url)"
                  :chooseFile="true"
                  :isDeleteFile="true"
                  @openImage="
                    openImageModal(previewBinary(file.url), file.name, 'choose')
                  "
                  @deleteFile="deleteFile(file.url, index, 'selectFile')"
                ></FilePreViewList>
              </li>
            </ul>
          </div>
        </div>

        <div class="flex flex-row justify-end gap-3 pl-10 pr-5 my-5">
          <div class="flex flex-col" v-show="!editMode">
            <div class="font-bold text-sm">Timezone</div>
            <div
              v-if="showLoading"
              class="animate-pulse bg-gray-300 rounded-md h-6 w-28 mt-2"
            ></div>
            <div v-else class="itbkk-timezone mt-2">
              {{ Intl.DateTimeFormat().resolvedOptions().timeZone }}
            </div>
          </div>
          <div class="flex flex-col" v-show="!editMode">
            <div class="font-bold text-sm">Created On</div>
            <div
              v-if="showLoading"
              class="animate-pulse bg-gray-300 rounded-md h-6 w-36 mt-2"
            ></div>
            <div v-else class="itbkk-created-on mt-2">
              {{ createDate }}
            </div>
          </div>
          <div class="flex flex-col" v-show="!editMode">
            <div class="font-bold text-sm">Updated On</div>
            <div
              v-if="showLoading"
              class="animate-pulse bg-gray-300 rounded-md h-6 w-36 mt-2"
            ></div>
            <div v-else class="itbkk-updated-on mt-2">
              {{ updateDate }}
            </div>
          </div>
        </div>

        <div
          class="w-full flex justify-end sticky bottom-0 z-10 bg-white"
          :class="editMode ? 'block ' : 'hidden'"
        >
          <div class="flex mr-5 my-2 gap-2">
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
                  $emit(
                    'addEdit',
                    duplicateTask,
                    previewImagesURL,
                    fileDetete.fileName
                  )
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
        class="itbkk-modal-task bg-gray-100 rounded-e-md h-[80%] w-[11rem] shadow-md border-l"
        :class="showAttachment ? '' : 'hidden'"
      >
        <div class="h-full relative">
          <!-- Close Button Container with sticky positioning -->
          <div class="absolute top-5 right-5 z-10">
            <div
              class="cursor-pointer text-error text-2xl h-[5%]"
              @click="$emit('userAction', false)"
            >
              <CloseIcon />
            </div>
          </div>
          <div
            class="flex flex-row items-end justify-between border-b sticky w-full h-[10%] shadow-sm"
          >
            <div class="font-bold text-base pl-2 mb-1">Attachments</div>

            <button
              @click="showRedo = true"
              v-show="showRedoButton"
              class="mb-1 pr-2"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="20"
                height="20"
                viewBox="0 0 24 24"
              >
                <g
                  fill="none"
                  stroke="currentColor"
                  stroke-linecap="round"
                  stroke-width="2"
                >
                  <path d="M4 9h12a5 5 0 0 1 5 5v0a5 5 0 0 1-5 5H7" />
                  <path stroke-linejoin="round" d="M7 5L3 9l4 4" />
                </g>
              </svg>
            </button>
          </div>
          <div class="overflow-y-auto h-[90%] pt-3">
            <AttachmentLoadingVue v-if="showLoadingFile" />
            <!-- Scrollable Content Area -->
            <div v-else>
              <div
                v-for="(file, index) in fileURL"
                :key="index"
                v-show="fileURL"
                class="flex flex-col w-full h-24 mb-3 px-2"
              >
                <FilePreViewList
                  :filename="file.name"
                  :fileurl="file.url"
                  :isDeleteFile="editMode || isEditPage"
                  @openImage="openImageModal(file.url, file.name, 'preview')"
                  @deleteFile="
                    deleteFile(file.url, index, 'fileDelete', file.name),
                      ((showRedoButton = true),
                      (numberFileCanRedo = numberFileCanAdd))
                  "
                  @downloadFile="downloadFile(file.name)"
                ></FilePreViewList>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <ConfirmModal
    v-if="showRedo"
    :width="'w-[60vh]'"
    :canEdit="boardStore.isCanEdit"
    :disabled="!fileSelectRedo.length > 0"
    @userAction="redoFile"
    class="z-50"
  >
    <template #header>
      <div
        class="flex flex-col justify-items-end place-items-end cursor-pointer text-rose-500"
        @click="redoFile(false)"
      >
        <CloseIcon />
      </div>
      <div class="flex justify-center">
        <span class="text-gray-800 font-bold text-[1.5rem]">
          Redo your file
        </span>
      </div>
    </template>
    <template #body>
      <div class="text-gray-800">Select the file which you need redo</div>
      <div class="mb-3 text-gray-500 text-sm">
        ** Files that cannot be redo due to duplicates or more than 10 files in total.
      </div>
      <div class="flex flex-row gap-3 flex-wrap justify-center">
        <div
          v-for="(file, index) of fileDetete.fileName.map((name, i) => ({
            fileName: name,
            fileUrl: fileDetete.fileUrl[i],
          }))"
          :key="index"
        >
          <input
            type="checkbox"
            :id="file.fileName"
            :value="{ name: file.fileName, url: file.fileUrl }"
            :disabled="
              fileSelectRedo.length > numberFileCanRedo ||
              previewImagesURL.find((e) => e.name === file.fileName)
            "
            v-model="fileSelectRedo"
            class="hidden peer"
          />

          <label
            :for="file.fileName"
            class="inline-flex items-center justify-between w-[8rem] h-[6rem] text-gray-500 bg-white border-2 border-gray-200 rounded-lg cursor-pointer peer-checked:border-blue-600 hover:text-gray-600 peer-checked:text-gray-600 hover:bg-gray-50"
            :class="
              fileSelectRedo.length > numberFileCanRedo ||
              previewImagesURL.find((e) => e.name === file.fileName)
                ? 'cursor-not-allowed opacity-50'
                : ''
            "
          >
            <FilePreViewList
              class="w-full h-full"
              :filename="file.fileName"
              :fileurl="file.fileUrl"
              :redofile="true"
            ></FilePreViewList>
          </label>
        </div>
      </div>
    </template>
  </ConfirmModal>
  <ImageViewer
    :imageSrc="selectedImage"
    :visible="showImageModal"
    :imageType="imageType"
    :showLoadingImage="showLoadingImage"
    @close="showImageModal = false"
  />

  <div
    class="z-50 absolute bottom-[1%] right-0 flex items-center justify-end mr-5 w-full"
    v-if="showToast"
  >
    <Toast :toast="typeToast" @close-toast="showToast = false">
      <template #message>
        <span class="itbkk-message break-all">{{ messageToast }}</span>
      </template>
    </Toast>
  </div>
</template>

<style scoped>
@media not all and (min-height: 830px) {
  .max-h-800px {
    height: 65%;
  }
}

.draggedover {
}

#overlay {
  display: none;
}

#overlay.draggedover {
  display: flex;
}
</style>
