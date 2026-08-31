# Reflections on AI-Assisted Software Engineering

Throughout development, there were a few prompts that were made which were either unclear or lead to misconceptions/false assumptions made by LLM. Some prompts which immediately asked for implementation without proper clarification of requirements or context also resulted in unecessary code being modified or generated.

Below are a few examples of such instances. Note that certain details may be omitted for simplicity.

## 1. GUI completion checkmark

Prompt:

```text
Next, in the requirements panel, I need an indicator for each item to display
completion status... If the requirement is completed, there should be a green
tick icon aligned to the right side of the requirements panel.
```

The prompt is used to request implementation of a feature in the GUI and it describes the desired appearance, but not how the result should be
verified. The model created and sized the icon but there was an issue with binding of component width which causes unintentional clipping of icon, so the green tick icon indicator is not displayed. Unit tests passed because they did not
test JavaFX rendering.

My follow-up prompt simply highlights the issue faced after LLM's attempt on implementation, which is that the green checkmark does not seem to be visible. This was too vague and led to several guesses about sizing and SVG rendering. Subsequent attempts from LLM at debugging strictly from the code have failed.
After which, I decided to create a new conversation and re-provided only the context of the bug. In the prompt, I requested the LLM to carry out the GUI debugging process step, where it generated an temporary file (`UiProbe.java`) and identified an oversized content width, allowing it to fix the bug.

When fixing GUI bugs, manual GUI testing seemed to be more helpful then simple automated testing here. While having less context may have helped in allowing the LLM to focus on debugging this issue, the structure of the prompt requiring the LLM to execute its own manual GUI debugging allowed it to identify the issue.

## 2. Corrupted JSON startup handling

Prompt:

```text
I modified the JSON data, changing an attribute from `modules` to `module`.
The JAR file cannot be launched and there were no error messages. 
- Verify what happens upon loading a corrupted data file.
- Make the application launch with an alert specifying the error message before loading the default data.
```

This prompt was made after manual testing, when I found that the application does not properly handle corrupted data file late into the development process. While the prompt is simple, the LLM model managed to easily trace the failure from JSON parsing through `StorageManager` to JavaFX startup, then added a fallback result and warning alert. The prompt was very effective because it included the exact malformed change, the observed symptom, and the desired recovery behaviour. 

Further review showed that corrupted data and file-permission errors should be
reported differently. I verified the fix with unit tests and a packaged-JAR
smoke test using the malformed JSON file.

This example showed that precise reproduction details and acceptance criteria
make debugging more focused, allowing LLM to more easily detect issues or execute tasks.

## 3. Application architecture proposal

The following prompt was made after defining the requirements and updating `AGENTS.md` with the relevant requirement contexts:

```text
Based on the given context and requirements, help me propose the components
and their functionalities required.
```

The model proposed a plausible but unnecessarily complex architecture with
`ApplicationSession`, `ProgressService`, multiple controllers, and additional
storage layers. These components were common software-engineering patterns,
but there are many unecessary components which introduced a lot of complexity and may not be easily achieveable within the project deadline. The current architecture (primarily consisting of `ModulesManager`, `RequirementsManager`, and `StorageManager`) is simpler and more reasonable for implementation within the timeframe without compromising on the base requirements.

I had to compare the proposal against the existing code, project scope, and
`AGENTS.md`, then decide which abstractions were actually justified. This was
an example where prompting produced many reasonable possibilities or overcomplicated the design. The final result ended with something that is close to an architecture which I manually engineered on my own rather than one which was proposed by LLM in order to control the scope of the project.

## Overall reflection

LLMs were effective for generating implementation ideas, tests, and likely
failure points, but they tended to fill in unspecified details using general
patterns. Clear acceptance criteria, small prompts, manual testing, and
verification against the project requirements were still necessary. Prompting
was least effective when I reported only a symptom or referred vaguely to an
earlier plan without repeating the relevant context.
