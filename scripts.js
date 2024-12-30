document.getElementById("theme-toggle").addEventListener("click", function () {
    const body = document.body;
    const themeIcon = document.getElementById("theme-icon");
    const toggleButton = document.querySelector(".theme-toggle-button");

    body.classList.toggle("black-background");
    body.classList.toggle("white-background");

    if (body.classList.contains("white-background")) {
        themeIcon.textContent = "🌙";
        toggleButton.style.backgroundColor = "#f4f4f4";
        toggleButton.style.color = "#000";
    } else {
        themeIcon.textContent = "☀️";
        toggleButton.style.backgroundColor = "#000";
        toggleButton.style.color = "#fff";
    }
});