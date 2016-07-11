

<style>
 .red-thing-vue {
     background-color: red;
 }
</style>

<template>
    <div class="red-thing-vue">
        <button></button>
    </div>
</template>

<script>
 module.exports = {
     ready: function() {
         console.log('the red thing is ready');
     }
 }
</script>
