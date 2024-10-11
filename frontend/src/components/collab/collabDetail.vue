<script setup>
import { computed, ref, watch } from "vue";
import CloseIcon from "../icon/CloseIcon.vue";
import { useRouter } from "vue-router";
import { validateSizeInput } from "../../lib/utill.js";
import { useUserStore } from "../../stores/user.js";


const boardStore = useUserStore();
defineEmits(["userAction", "addEdit"]);
const props = defineProps({
  errorMSG: {
    type: String,
    default: "",
  },
  showLoading: {
    type: Boolean,
  },
  username: {
    type: String,
  },
});
const userStore = useUserStore();
const router = useRouter();
const validate = ref({ email: {} });
const accessSelect = ref("Read");
const email = ref("");
const errorMSG = ref(props.errorMSG);

let accessRight = ref(["Read", "Write"]);

const isFormValid = computed(() => {
  const arrStyle = validateSizeInput({
    propName: "Email",
    propLenght: email.value.trim().length,
    size: 50,
  });
  validate.value.email = arrStyle[0];
  const emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;
  const isEmailValid = emailPattern.test(email.value.trim());
  const isOwner = email.value.trim() === userStore.authToken.email;

  return (
    validate.value.email.boolean === false &&
    email.value.trim() !== "" &&
    accessSelect.value !== "" &&
    isEmailValid &&
    !isOwner
  );
});

watch(
  () => email.value,
  () => {
    if(email.value.trim() ===userStore.authToken.email){
      errorMSG.value = "Board owner cannot be a collaborator of his/her own board."; //check if email is owner
    }
    else{
      errorMSG.value = ""; // clear error message
    }
  }
);

watch(
  () => props.errorMSG,
  () => {
    errorMSG.value = props.errorMSG;
  }
); // watch error message ถ้าไม้ใส่ไว้จะไม่แสดง error message

</script>

<template>
  <div
    class="absolute left-0 right-0 m-auto top-0 bg-black h-screen w-screen bg-opacity-50 z-50"
  >
    <div class="flex h-full items-center justify-center">
      <div
        class="itbkk-modal-board flex flex-col justify-start rounded-md bg-white h-[24%] w-[45rem] max-lg:w-[35rem] max-lg:h-[29%] max-md:w-[30rem] max-md:h-[29%] max-sm:w-[25rem] max-sm:h-[30%] shadow-md relative overflow-auto"
      >
        <!-- Close Button -->
        <div class="w-full flex justify-end">
          <div
            class="cursor-pointer text-error text-2xl text-wrap pt-5 pr-5"
            @click="$emit('userAction', false)"
          >
            <CloseIcon />
          </div>
        </div>

        <!-- Content -->
        <div class="flex flex-col w-full pl-9 mt-1">
          <!-- Status Name -->
          <div class="flex flex-col w-fit">
            <span class="font-bold text-xl"> Add Collaborater</span>
            <div class="flex flex-row items-center gap-2 w-fit flex-wrap">
              <div class="flex flex-col">
                <label class="font-medium">Collaborater e-mail</label>
                <input
                  class="itbkk-collaborater-email read-only:focus:outline-none placeholder:text-gray-500 placeholder:italic break-all mt-2 p-2 rounded-lg border border-gray-800 h-full resize-none w-[30rem] max-sm:w-[20rem] max-md:w-[25rem]"
                  type="email"
                  name="statusName"
                  id="statusName"
                  v-model="email"
                  maxlength="50"
                  placeholder="example@ad.sit.kmutt.ac.th"
                />
                <span class="text-xs text-red-500 h-[0.1rem]" v-if="errorMSG">
                  {{ errorMSG }}
                </span>
              </div>
              <div class="flex flex-col items-start">
                <label class="font-medium">Access Right</label>
                <div class="itbkk-acess-right">
                  <select
                    class="itbkk-status border-2 border-gray-500 w-[10rem] h-[40px] mt-2 rounded-lg"
                    v-model="accessSelect"
                  >
                    <option v-for="access in accessRight">
                      {{ access }}
                    </option>
                  </select>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Save Buttons, Close Buttons -->
        <div class="w-full flex justify-end mb-5 mt-4">
          <div class="flex mr-10 gap-2">
            <button
              class="itbkk-button-ok text-white inline-flex items-center focus:ring-4 focus:outline-none font-medium rounded-lg text-sm px-5 py-2.5 text-center"
              :class="
                isFormValid
                  ? 'cursor-pointer bg-green-700 hover:bg-green-800'
                  : 'bg-gray-300 cursor-not-allowed'
              "
              :disabled="!isFormValid "
              @click="$emit('addEdit', { email: email, access: accessSelect })"
            >
              ADD
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
    </div>
  </div>
</template>

<style scoped>
@media not all and (max-height: 850px) {
  .max-h-800px {
    height: 50%;
  }
}
</style>
