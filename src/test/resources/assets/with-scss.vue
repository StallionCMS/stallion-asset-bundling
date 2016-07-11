<style lang="scss">
    @import '_partial_for_vue';
    $color: green;
    .with-scss {
        h1, h2 {
            font-family: comic sans, avenir;
            color: $color;
            background-color: $bg-color;
        }
    }
</style>

<script>
 module.exports = {
     ready: function() {
         console.log("I am here!");
     }
 }
</script>

<template>
   <div class="with-scss">
       <h1>Header</h1>
       <h2>Sub-Header</h2>
   </div>
</template>