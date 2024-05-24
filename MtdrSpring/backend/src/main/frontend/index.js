const express = require('express');
const { OpenAI } = require('openai');
require('dotenv').config();


// Assuming you have set your API key in.env file or directly in your environment
const openaiClient = new OpenAI({
  apiKey: "b1d77c735e99e18d7b5494bd00e9bef4feea2ba5b42ed73460b6305cc19df2ed",
  baseURL: 'https://api.together.xyz/v1',
});

async function askSoftwareDevelopmentQuestion(question){
  const response = await openaiClient.chat.completions.create({
    messages: [
      {
        role: 'system',
        content: 'You are an expert in software development.',
      },
      {
        role: 'user',
        content: question,
      },
    ],
    model: 'mistralai/Mixtral-8x7B-Instruct-v0.1',
  });

  return response.choices[0].message.content;
}

const app = express();
app.use(express.json());

app.post('/ask', async (req, res) => {
  try {
    const question = req.body.question;
    if (!question) {
      return res.status(400).send('Missing question parameter');
    }
    const answer = await askSoftwareDevelopmentQuestion(question);
    res.send({ answer });
  } catch (error) {
    res.status(500).send(error.message);
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`App successfully started on PORT:${PORT}`);
});
