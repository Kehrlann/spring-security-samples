package wf.garnier.security.method;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class MethodApplicationTests {

    @Autowired
    MockMvc mvc;

    @Test
    void one(CapturedOutput output) throws Exception {
        mvc.perform(get("/one")).andExpect(status().isOk());

        assertThat(output).contains("SomeString: hello");
    }

    @Test
    void two(CapturedOutput output) throws Exception {
        mvc.perform(get("/two")).andExpect(status().isOk());

        assertThat(output).contains("Authentication: ")
                .contains("SomeStringArray: [one, two]");
    }

    @Test
    void three(CapturedOutput output) throws Exception {
        mvc.perform(get("/three")).andExpect(status().isOk());

        assertThat(output).contains("Authentication: ")
                .contains("SomeStringArray: [one, two]");
    }

    @Test
    void four(CapturedOutput output) throws Exception {
        mvc.perform(get("/four")).andExpect(status().isOk());

        assertThat(output).contains("Authentication: ")
                .contains("SomeStringArray: [one, two]");
    }

    @Test
    void five(CapturedOutput output) throws Exception {
        mvc.perform(get("/five")).andExpect(status().isOk());

        assertThat(output).contains("Authentication: ")
                .contains("SomeString: hello")
                .contains("SomeBoolean: false")
                .contains("SomeStringArray: [one, two]");
    }

    @Test
    void six(CapturedOutput output) throws Exception {
        mvc.perform(get("/six")).andExpect(status().isOk());

        assertThat(output).contains("Permissions: [CREATE, READ]");
    }

}
