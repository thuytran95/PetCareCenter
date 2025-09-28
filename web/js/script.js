const avatar = document.querySelectorAll('.avatar');
avatar.forEach((item) => {
    item.addEventListener('click', () => {
      const image = item.querySelector('img');     
      const url = image.getAttribute('src');
      const tile = image.getAttribute('alt');
      const modalImage = document.querySelector('.modal-body img');
      const modalTitle = document.querySelector('.modal-title');
        modalTitle.innerText = tile;
        modalImage.src = url;
    }); 
});