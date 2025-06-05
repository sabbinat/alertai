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