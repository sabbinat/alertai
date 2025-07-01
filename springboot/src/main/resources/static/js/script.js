
// -------------------------------------
// 2. Mostrar u ocultar la contraseña
// -------------------------------------

document.addEventListener('DOMContentLoaded', function () {
  const passwordInput = document.getElementById('password');
  const toggleIcon = document.getElementById('togglePassword');

  if (toggleIcon && passwordInput) {
    toggleIcon.addEventListener('click', function () {
      const isPassword = passwordInput.type === 'password';
      passwordInput.type = isPassword ? 'text' : 'password';
      this.classList.toggle('bi-eye');
      this.classList.toggle('bi-eye-slash');
    });
  }
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
  if (!carouselEl) return; 

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
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('[id^="drop-area-edit-"]').forEach((dropArea) => {
  const id = dropArea.id.split('drop-area-edit-')[1];
  const imageInput = document.getElementById(`image-edit-${id}`);
  const previewImg = document.getElementById(`preview-edit-${id}`);
  const dropPlaceholder = document.getElementById(`drop-placeholder-edit-${id}`);
  const modal = dropArea.closest('.modal'); 

  // Click en dropArea abre input
  dropArea.addEventListener('click', () => {
    imageInput.click();
  });

  // Cargar imagen y previsualizar
  imageInput.addEventListener('change', (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = () => {
        previewImg.src = reader.result;
        dropPlaceholder?.classList.add('d-none');
      };
      reader.readAsDataURL(file);
    }
  });

  // Restaurar imagen original al cerrar el modal
  if (modal) {
    modal.addEventListener('hidden.bs.modal', () => {
      // Restaurar imagen original
      const originalSrc = previewImg.dataset.originalSrc;
      if (originalSrc) previewImg.src = originalSrc;

      // Mostrar placeholder si es necesario
      if (dropPlaceholder && !originalSrc) {
        dropPlaceholder.classList.remove('d-none');
      }

      // Limpiar input de archivo
      imageInput.value = '';
    });
  }
});
});





// -------------------------------------
// FUNCIONES DE EDICIÓN DE PERFIL 

// Previsualizar imagen al seleccionar archivo
document.querySelectorAll('input.overlay-input').forEach(input => {
  const preview = document.getElementById(input.dataset.previewId);
  if (!preview) return;

  // Guardar la imagen original si aún no está guardada
  if (!preview.dataset.originalSrc) {
    preview.dataset.originalSrc = preview.src;
  }

  input.addEventListener('change', () => {
    const file = input.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = e => {
        preview.src = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  });
});

// Restaurar imagen original si se cierra el modal sin guardar
document.querySelectorAll('.modal').forEach(modal => {
  modal.addEventListener('hidden.bs.modal', () => {
    modal.querySelectorAll('input.overlay-input').forEach(input => input.value = '');
    modal.querySelectorAll('img[data-original-src]').forEach(img => {
      img.src = img.dataset.originalSrc; 
    });
  });
});


// -------------------------------------
// GESTIONA EL DRAG AND DROP PARA SELECCIONAR IMAGEN 

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

// Soltar archivo
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


// MUESTRA LA VISTA PREVIA DE LA IMAGEN SELECCIONADA
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
// 9. TOOLTIPS PARA LAS COLUMNAS DE LAS TABLAS 

document.addEventListener("DOMContentLoaded", function () {
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
    tooltipTriggerList.forEach(function (tooltipTriggerEl) {
      new bootstrap.Tooltip(tooltipTriggerEl)
    })
});

// --------------------------------------
// MUESTRA REDES SOCIALES EN COMPARTIR EVENTO (EVENT.HTML)

function toggleSocialRow(event) {
    event.preventDefault();
    const row = document.getElementById("socialRow");
    row.classList.toggle("d-none");
  }

// ABRE EL COMMENT / REPLAY FORM
function toggleCommentForm() {
  const commentForm = document.querySelector(".comment-form");
  if (commentForm) {
    commentForm.style.display = commentForm.style.display === "none" ? "block" : "none";
  }
}

function toggleReplyForm(link) {
  const replyForm = link.nextElementSibling;
  if (replyForm.style.display === 'none' || replyForm.style.display === '') {
    replyForm.style.display = 'block';
  } else {
    replyForm.style.display = 'none';
  }
}

// --------------------------------------
// ABRE SIDEBAR EN USER-HOME
// --------------------------------------  
const openSidebarBtn = document.getElementById('openSidebarBtn');
const closeSidebarBtn = document.getElementById('closeSidebarBtn');

if (openSidebarBtn) {
  openSidebarBtn.addEventListener('click', function () {
    document.getElementById('sidebarNoticias').classList.add('open');
  });
}

if (closeSidebarBtn) {
  closeSidebarBtn.addEventListener('click', function () {
    document.getElementById('sidebarNoticias').classList.remove('open');
  });
}


// --------------------------------------
// SE ABRE UN CAMPO PARA AGREGAR OTRA CATEGORÍA
// --------------------------------------  
document.addEventListener('DOMContentLoaded', function() {
    const categorySelect = document.getElementById('categoryId');
    const customCategoryDiv = document.getElementById('customCategoryDiv');
    const customCategoryInput = document.getElementById('customCategory');

    categorySelect.addEventListener('change', function() {
      if (this.value === '9') {  // id "Otro"
        customCategoryDiv.style.display = 'block';
        customCategoryInput.required = true;
      } else {
        customCategoryDiv.style.display = 'none';
        customCategoryInput.required = false;
        customCategoryInput.value = '';
      }
    });
  });


// --------------------------------------
// PANEL DE NOTIFICACIONES 
// --------------------------------------  
document.addEventListener("DOMContentLoaded", () => {
  const toggleBtn = document.getElementById("toggleNotifications");
  const panel = document.getElementById("notificationPanel");

  toggleBtn.addEventListener("click", () => {
    const isVisible = panel.classList.contains("show");
    
    if (isVisible) {
      panel.classList.remove("show");
    } else {
      panel.classList.add("show");

      // Ocultar badge
      const badge = toggleBtn.querySelector(".badge");
      if (badge) badge.style.display = "none";

      // Marcar como leídas en el backend
      fetch("/user/notificaciones/vistas", {
        method: "POST",
        headers: {
          "X-Requested-With": "XMLHttpRequest",
          "Content-Type": "application/json"
        }
      });
    }
  });
});