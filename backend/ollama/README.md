# Ollama Setup

Study Buddy uses Ollama with the `qwen3:4b` model

## Install Ollama

On MacOS with Homebrew
```bash
brew install ollama
```

On Windows with WinGet:

```powershell
winget install Ollama.Ollama
```

## Start Ollama and download the model:

```bash
ollama serve # start Ollama
ollama pull qwen3:4b # download model
```

Verify the model is installed:

```bash
ollama list
```

Start Ollama (`ollama serve`) before starting Study Buddy
