package svsite.matzip.foody.domain.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import svsite.matzip.foody.domain.auth.api.AuthController;
import svsite.matzip.foody.domain.auth.service.AuthService;
import svsite.matzip.foody.domain.favorite.api.FavoriteController;
import svsite.matzip.foody.domain.favorite.service.FavoriteService;
import svsite.matzip.foody.domain.image.api.ImageController;
import svsite.matzip.foody.domain.post.api.PostController;
import svsite.matzip.foody.domain.post.service.PostService;
import svsite.matzip.foody.global.auth.AuthenticatedUserResolver;
import svsite.matzip.foody.global.util.file.service.FileUploadService;

@WebMvcTest(controllers = {
    AuthController.class,
    PostController.class,
    ImageController.class,
    FavoriteController.class
})
public abstract class ControllerTestSupport {
  @Autowired
  protected MockMvc mockMvc;
  @Autowired
  protected ObjectMapper objectMapper;
  @MockBean
  protected AuthService authService;
  @MockBean
  protected AuthenticatedUserResolver authenticatedUserResolver;
  @MockBean
  protected PostService postService;
  @MockBean
  protected FileUploadService fileUploadService;
  @MockBean
  protected FavoriteService favoriteService;

}
