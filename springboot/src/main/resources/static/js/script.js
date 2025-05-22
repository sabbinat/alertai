// -------------------------------------
// 1. Configuración de tema oscuro/claro
// -------------------------------------

document.addEventListener('DOMContentLoaded', () => {
  const btn  = document.getElementById('theme-toggle-btn');
  const icon = document.getElementById('theme-icon');
  const body = document.body;

  // 1) Carga tema guardado o predeterminado
  const saved = localStorage.getItem('theme');
  const isDark = saved === 'dark';
  body.classList.add(isDark ? 'theme-dark' : 'theme-light');

  // 2) Función para actualizar el icono y tooltip
  function updateIcon() {
    if (body.classList.contains('theme-light')) {
      icon.classList.replace('bi-lightbulb', 'bi-lightbulb-fill');
      btn.title = 'Cambiar a modo oscuro';
    } else {
      icon.classList.replace('bi-lightbulb-fill', 'bi-lightbulb');
      btn.title = 'Cambiar a modo claro';
    }
  }
  updateIcon();

  // 3) Al clicar, alterna clases y guarda en localStorage
  btn.addEventListener('click', () => {
    body.classList.toggle('theme-light');
    body.classList.toggle('theme-dark');
    localStorage.setItem(
      'theme',
      body.classList.contains('theme-dark') ? 'dark' : 'light'
    );
    updateIcon();
  });
});


// -------------------------------------
// 2. Mostrar u ocultar la contraseña
// -------------------------------------

document.addEventListener('DOMContentLoaded', function () {
  const passwordInput = document.getElementById('password');
  const toggleIcon = document.getElementById('togglePassword');

  // Cambiar el tipo de la contraseña (de "password" a "text" y viceversa)
  toggleIcon.addEventListener('click', function () {
    const isPassword = passwordInput.type === 'password';
    passwordInput.type = isPassword ? 'text' : 'password';
    // Cambiar el icono (ojo abierto/cerrado)
    this.classList.toggle('bi-eye');
    this.classList.toggle('bi-eye-slash');
  });
});


// -------------------------------------
// 3. Mostrar campos de alerta según categoría seleccionada
// -------------------------------------

document.addEventListener('DOMContentLoaded', function () {
  const categorySelect = document.getElementById('categoryId');
  console.log('categorySelect:', categorySelect); // Depuración
  
  if (!categorySelect) {
    console.error('El elemento con id="categoryId" no se encontró en el DOM.');
    return;
  }

  const alertFields = document.querySelectorAll('.alert-only');
  
  // Función para mostrar u ocultar los campos de alerta
  function toggleAlertFields() {
    const selectedOption = categorySelect.options[categorySelect.selectedIndex];
    const categoryName = selectedOption.getAttribute('data-name');
    console.log('Categoría seleccionada:', categoryName);

    if (categoryName && categoryName.trim().toLowerCase() === 'alerta') {
      alertFields.forEach(el => el.style.display = 'block');
    } else {
      alertFields.forEach(el => el.style.display = 'none');
    }
  }

  // Event listener para el cambio de categoría
  categorySelect.addEventListener('change', toggleAlertFields);
  toggleAlertFields(); // Llamar para inicializar la visibilidad de los campos
});

// -------------------------------------
// 4. Vista previa de la imagen de perfil
// -------------------------------------

function previewProfileImage(event) {
  const img = event.target.previousElementSibling;
  img.src = URL.createObjectURL(event.target.files[0]);
}



// -------------------------------------
// 5. Actualización de flechas en el carrusel
// -------------------------------------

function updateCarouselArrows(carouselId, prevBtnId, nextBtnId) {
  const carouselEl = document.querySelector(carouselId);
  const carousel = bootstrap.Carousel.getInstance(carouselEl) 
                  || new bootstrap.Carousel(carouselEl, { interval: false });

  const prevBtn = document.getElementById(prevBtnId);
  const nextBtn = document.getElementById(nextBtnId);

  // Escuchar el evento de cambio de elemento en el carrusel
  carouselEl.addEventListener('slid.bs.carousel', () => {
    const activeIndex = carouselEl.querySelector('.carousel-item.active');
    const allItems = [...carouselEl.querySelectorAll('.carousel-item')];
    const currentIndex = allItems.indexOf(activeIndex);

    // Mostrar u ocultar botones según el índice
    prevBtn.style.display = currentIndex === 0 ? 'none' : 'block';
    nextBtn.style.display = currentIndex === allItems.length - 1 ? 'none' : 'block';
  });

  // Llamar una vez para inicializar correctamente
  carouselEl.dispatchEvent(new Event('slid.bs.carousel'));
}

document.addEventListener('DOMContentLoaded', () => {
  updateCarouselArrows('#carouselMyEvents', 'prevMyEvents', 'nextMyEvents');
  updateCarouselArrows('#carouselRecentEvents', 'prevRecentEvents', 'nextRecentEvents');
});



// -------------------------------------
// 6. Gestión de imágenes para los modales
// -------------------------------------

// Función reutilizable para limpiar la vista previa de imagen
function limpiarImagen(imageInput, previewImg, dropPlaceholder) {
  if (imageInput) imageInput.value = "";
  if (previewImg) {
    previewImg.classList.add('d-none');
    previewImg.src = "#";
  }
  if (dropPlaceholder) {
    dropPlaceholder.classList.remove('d-none');
  }
}

// -------------------------------------
// 6.1 Modal de Crear Evento
// -------------------------------------

const crearEventoModal = document.getElementById('crearEventoModal');

if (crearEventoModal) {
  const imageInputCrear = document.getElementById('image');
  const previewImgCrear = document.getElementById('preview');
  const dropPlaceholderCrear = document.getElementById('drop-placeholder');

  // Al cerrar el modal de crear evento
  crearEventoModal.addEventListener('hidden.bs.modal', () => {
    limpiarImagen(imageInputCrear, previewImgCrear, dropPlaceholderCrear);
  });

  // Al abrir el modal de crear evento (para actualizar el mapa)
  crearEventoModal.addEventListener('shown.bs.modal', () => {
    setTimeout(() => {
      if (typeof map !== 'undefined') {
        map.invalidateSize();
      }
    }, 200);
  });
}

// -------------------------------------
// 6.2 Modales de Editar Evento 
// -------------------------------------

document.querySelectorAll('[id^="drop-area-edit-"]').forEach((dropArea) => {
  const id = dropArea.id.split('drop-area-edit-')[1];
  const imageInput = document.getElementById(`image-edit-${id}`);
  const previewImg = document.getElementById(`preview-edit-${id}`);
  const dropPlaceholder = document.getElementById(`drop-placeholder-edit-${id}`);

  dropArea.addEventListener('click', () => {
    imageInput.click();
  });

  imageInput.addEventListener('change', (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = () => {
        previewImg.src = reader.result;
        dropPlaceholder.classList.add('d-none');
      };
      reader.readAsDataURL(file);
    }
  });
});

// -------------------------------------
// 6.3 Modales de Editar perfil 
// -------------------------------------

document.querySelectorAll('input.overlay-input').forEach(input => {
  const preview = document.getElementById(input.dataset.previewId);
  if (!preview) return;

  input.addEventListener('change', () => {
    if (input.files && input.files[0]) {
      const reader = new FileReader();
      reader.onload = e => { preview.src = e.target.result; };
      reader.readAsDataURL(input.files[0]);
    }
  });
});

const modal = document.getElementById('editarPerfilModal');
if (modal) {
  modal.addEventListener('hidden.bs.modal', () => {
    const input  = modal.querySelector('input.overlay-input');
    const preview= modal.querySelector('img#preview-user-image');
    if (input)   input.value = '';
    if (preview) preview.src = preview.dataset.originalSrc || '#';
  });
}





// -------------------------------------
// 7. Inicialización de mapa y geolocalización
// -------------------------------------
let map = L.map('map-form').setView([-30.9059, -55.5501], 13);
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  attribution: '© OpenStreetMap'
}).addTo(map);
// Variable para guardar el marcador
let marker;
// Función que usa Nominatim para geocodificar una dirección
function geocodeAddress() {
  const address = document.getElementById('address').value;

  fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}`)
    .then(response => response.json())
    .then(data => {
      if (data && data.length > 0) {
        const lat = data[0].lat;
        const lon = data[0].lon;

        // Eliminar marcador anterior si existe
        if (marker) map.removeLayer(marker);

        // Crear un nuevo marcador arrastrable
        marker = L.marker([lat, lon], { draggable: true }).addTo(map)
          .bindPopup('Ubicación encontrada').openPopup();

        // Centrar el mapa
        map.setView([lat, lon], 15);

        // Establecer coordenadas en campos ocultos
        document.getElementById('latitude').value = lat;
        document.getElementById('longitude').value = lon;

        // Actualizar coordenadas si se arrastra el marcador
        marker.on('dragend', function (e) {
          const newLatLng = e.target.getLatLng();
          document.getElementById('latitude').value = newLatLng.lat;
          document.getElementById('longitude').value = newLatLng.lng;
        });

      } else {
        alert('Dirección no encontrada.');
      }
    })
    .catch(err => {
      alert('Error al buscar la dirección');
      console.error(err);
    });
}


// -------------------------------------
// 7.1. Inicialización de mapa y geolocalización en Event edit
// -------------------------------------
function initEditMap(eventId) {
  console.log("Ejecutando initEditMap para evento:", eventId);

  const mapDiv = document.getElementById("map-edit-" + eventId);
  const lat = parseFloat(document.getElementById("latitude-edit-" + eventId).value);
  const lon = parseFloat(document.getElementById("longitude-edit-" + eventId).value);

  if (!mapDiv) {
    console.error("No se encontró el div del mapa.");
    return;
  }

  // Si ya existe un mapa en ese div, eliminarlo
  if (mapDiv._leaflet_id) {
    mapDiv._leaflet_id = null;    // elimina el id de leaflet en el div
    mapDiv.innerHTML = "";        // limpia el contenido del div
  }

  const map = L.map(mapDiv).setView([lat, lon], 13);

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors'
  }).addTo(map);

  L.marker([lat, lon]).addTo(map).bindPopup('Ubicación del evento').openPopup();


  // 👇 Marcador arrastrable
  const marker = L.marker([lat, lon], { draggable: true }).addTo(map)
    .bindPopup('Ubicación del evento').openPopup();

  // 👇 Actualiza los campos ocultos al arrastrar el marcador
  marker.on('dragend', function (e) {
    const newLatLng = e.target.getLatLng();
    document.getElementById("latitude-edit-" + eventId).value = newLatLng.lat;
    document.getElementById("longitude-edit-" + eventId).value = newLatLng.lng;
  });
  setTimeout(() => {
    map.invalidateSize();
  }, 0);
}
function geocodeAddressEdit(eventId) {
  const addressInput = document.getElementById("address-edit-" + eventId);
  const latInput = document.getElementById("latitude-edit-" + eventId);
  const lonInput = document.getElementById("longitude-edit-" + eventId);
  const addressDisplay = document.getElementById("event-address-edit-" + eventId);

  fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(addressInput.value)}`)
    .then(response => response.json())
    .then(data => {
      if (data && data.length > 0) {
        const lat = parseFloat(data[0].lat);
        const lon = parseFloat(data[0].lon);
        latInput.value = lat;
        lonInput.value = lon;
        if (addressDisplay) addressDisplay.innerText = addressInput.value;
        initEditMap(eventId);
      }
    });
}
document.addEventListener('DOMContentLoaded', () => {
  const modals = document.querySelectorAll('[id^="editarEventoModal__"]');

  modals.forEach(modal => {
    const eventId = modal.id.split('__')[1];

    modal.addEventListener('shown.bs.modal', () => {
      initEditMap(eventId);
    });
  });
});


// -------------------------------------
// 8. Drag & Drop para seleccionar imagen
// -------------------------------------

const dropArea = document.getElementById('drop-area');
const inputImage = document.getElementById('image');

// Permitir abrir selector de archivos haciendo clic en el área
dropArea.addEventListener('click', () => inputImage.click());

// Estilo visual al arrastrar
dropArea.addEventListener('dragover', (e) => {
  e.preventDefault();
  dropArea.classList.add('bg-light');
});
dropArea.addEventListener('dragleave', () => {
  dropArea.classList.remove('bg-light');
});

// Soltar archivo: asignarlo al input
dropArea.addEventListener('drop', (e) => {
  e.preventDefault();
  dropArea.classList.remove('bg-light');
  inputImage.files = e.dataTransfer.files;
});

// Mostrar vista previa de la imagen seleccionada
const fileInput = document.getElementById('image');
const preview = document.getElementById('preview');

// Manejar cambios en el input
fileInput.addEventListener('change', handleFile);

// Reacciones visuales al arrastrar sobre dropArea
dropArea.addEventListener('dragover', e => {
  e.preventDefault();
  dropArea.classList.add('border-primary');
});
dropArea.addEventListener('dragleave', () => {
  dropArea.classList.remove('border-primary');
});

// Manejar archivos soltados en el área
dropArea.addEventListener('drop', e => {
  e.preventDefault();
  dropArea.classList.remove('border-primary');

  if (e.dataTransfer.files.length) {
    fileInput.files = e.dataTransfer.files; // Transferir archivo
    handleFile();
  }
});


// Mostrar vista previa de la imagen seleccionada
function handleFile() {
  const file = fileInput.files[0];
  if (file && file.type.startsWith('image/')) {
    const reader = new FileReader();
    reader.onload = e => {
      preview.src = e.target.result;
      preview.classList.remove('d-none');

      // Ocultar texto e ícono
      const placeholder = document.getElementById('drop-placeholder');
      if (placeholder) {
        placeholder.classList.add('d-none');
      }
    };
    reader.readAsDataURL(file);
  }
}

// -------------------------------------
// 8.1 Respuesta a comentarios
// -------------------------------------
function toggleReplyForm(link) {
    const replyForm = link.nextElementSibling;
    if (replyForm.style.display === 'none' || replyForm.style.display === '') {
      replyForm.style.display = 'block';
    } else {
      replyForm.style.display = 'none';
    }
  }
  

// -------------------------------------
// 9. Inicialización de tooltips para las columnas de las tablas
// -------------------------------------
document.addEventListener("DOMContentLoaded", function () {
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
    tooltipTriggerList.forEach(function (tooltipTriggerEl) {
      new bootstrap.Tooltip(tooltipTriggerEl)
    })
  });