function previewImages() {
    const files = document.getElementById('images').files;
    const imageSlots = document.querySelectorAll('.image-slot');

    for (let i = 0; i < files.length; i++) {
        if (i >= 6) break; // Limit to 6 images (2x3 grid)
        
        const reader = new FileReader();
        reader.onload = function(e) {
            const img = document.createElement('img');
            img.src = e.target.result;
            imageSlots[i].innerHTML = ''; // Clear any previous content
            imageSlots[i].appendChild(img); // Add the image preview to the grid slot
        }
        reader.readAsDataURL(files[i]);
    }
}