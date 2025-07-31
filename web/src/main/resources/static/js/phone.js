    const input = document.querySelector("#phone");
    const iti = window.intlTelInput(input, {
        initialCountry: "auto",
        utilsScript: "https://cdn.jsdelivr.net/npm/intl-tel-input@18.1.1/build/js/utils.js",
        geoIpLookup: callback => {
            fetch('https://ipapi.co/json')
                .then(res => res.json())
                .then(data => callback(data.country_code))
                .catch(() => callback('us'));
        }
    });

    // Optional: update the hidden field with the full international number on submit
    const form = input.closest("form");
    form.addEventListener("submit", () => {
        input.value = iti.getNumber(); // e.g. +14155552671
    });