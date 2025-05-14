import 'package:flutter/material.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key, required this.title});

  final String title;

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  TextEditingController tec = TextEditingController();
  bool _ready = false;

  @override
  Widget build(BuildContext context) {

    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Container(
        margin: const EdgeInsets.fromLTRB(20, 20, 20, 20),
        width: double.infinity,
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            SizedBox(
              width: 200,
              height: 200,
              child: Image.asset("assets/images/question.png"),
            ),
            const SizedBox(
              height: 40,
            ),  
            TextField(
              decoration:
                  const InputDecoration(hintText: "¿Cuál es tu nombre?"),
              onChanged: (value) {
                _ready = value.isNotEmpty;
                setState(() {});
              },
              controller: tec,
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _ready
            ? () =>
                Navigator.of(context).pushNamed('/guess', arguments: tec.text)
            : null,
        backgroundColor: _ready ? Colors.blue : Colors.grey,
        child: const Icon(Icons.navigate_next),
      ),
    );
  }
}
