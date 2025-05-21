//
//  EjerciciosClase15.playground
//  Ejercicios clase 15
//
//  Created by Mauricio Cubides on 30/10/23.
//

import Foundation

// Ejercicio 1

// 1. Utilizando la herramienta PlayGround de Xcode construya un diccionario que contenga los productos que se muestran a continuación:
let products: [String: Int] = [
    "Apple Watch 6": 300,
    "iPhone 12": 749,
    "OnePlus 9": 500,
    "iMac24": 1400,
    "PlayStation 5": 500,
    "Macbook": 1700,
    "FitBit Versa": 200
]

// 2. Construya un ciclo que itere sobre los productos y calcule el valor total de los productos que valen menos de $1000.
var totalValue: Int = 0

for (_, price) in products {
    if price < 1000 {
        totalValue += price
    }
}

print("Valor total de los productos que valen menos de $1000: $\(totalValue)")


// Ejercicio 2

// 1. Construya un arreglo con 8 emojis de su preferencia.
let emojis = ["😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣"]

// 2. Con base en un número aleatorio, escoja una posición del arreglo.
let randomNum = Int.random(in: 0..<8)

// 3. Muestre en pantalla el emoji seleccionado aleatoriamente y el nombre del emoji en texto claro en español.
let selectedEmoji = emojis[randomNum]
var emojiName = ""

switch selectedEmoji {
    case "😀":
        emojiName = "Cara sonriente con ojos abiertos"
    case "😃":
        emojiName = "Cara sonriente con ojos grandes"
    case "😄":
        emojiName = "Cara sonriente con ojos cerrados y boca abierta"
    case "😁":
        emojiName = "Cara sonriente con ojos abiertos y cejas levantadas"
    case "😆":
        emojiName = "Cara sonriente con ojos cerrados y boca abierta con lágrimas"
    case "😅":
        emojiName = "Cara sonriente con ojos cerrados y gotas de sudor"
    case "😂":
        emojiName = "Cara sonriente con ojos cerrados y lágrimas de risa"
    case "🤣":
        emojiName = "Cara torcida de la risa"
    default:
        emojiName = "Emoji desconocido"
}

print("Emoji seleccionado: \(selectedEmoji)")
print("Nombre del emoji: \(emojiName)")

// Alternativa con diccionarios
let emojiDict: [String: String] = [
    "😀": "Cara sonriente con ojos abiertos",
    "😃": "Cara sonriente con ojos grandes",
    "😄": "Cara sonriente con ojos cerrados y boca abierta",
    "😁": "Cara sonriente con ojos abiertos y cejas levantadas",
    "😆": "Cara sonriente con ojos cerrados y boca abierta con lágrimas",
    "😅": "Cara sonriente con ojos cerrados y gotas de sudor",
    "😂": "Cara sonriente con ojos cerrados y lágrimas de risa",
    "🤣": "Cara torcida de la risa"
]

let randomNum2 = Int.random(in: 0..<emojiDict.count)

var currentIndex = 0
for (key, value) in emojiDict {
    if currentIndex == randomNum2 {
        print("Emoji seleccionado: \(key)")
        print("Nombre del emoji: \(value)")
        break
    }
    currentIndex += 1
}

// Otra alternativa
let randomNum3 = Int.random(in: 0..<emojiDict.count)
let selectedEmoji3 = Array(emojiDict.keys)[randomNum3]
print("Emoji seleccionado: \(selectedEmoji3)")
if let textEmoji = emojiDict[selectedEmoji3] {
    print("Nombre del emoji: \(textEmoji)")
}


// Ejercicio 3

// Construya una función para que dado un arreglo de enteros como el siguiente, genere un arreglo con los números menores a 20. Pruebe el funcionamiento e imprima el resultado en la consola
let values = [11, 43, 56, 12, 5, 6, 74]

func filterNumbers(numbers: [Int]) -> [Int] {
    var filteredNumbers = [Int]()
        for number in numbers {
            if number < 20 {
                filteredNumbers.append(number)
            }
        }
    return filteredNumbers
}

let result = filterNumbers(numbers: values)
print("Números menores a 20: \(result)")

// Itilizando filter()
func filterNumbers2(numbers: [Int]) -> [Int] {
    let numbers = numbers.filter {$0 < 20}
    return numbers
}


// Ejercicio 4
// Utilizando el mismo arreglo del Ejercicio 1 desarrolle:

// 1. Una función que retorne los artículos por debajo de mil dólares (Filter).
func productsBelowPrice (products: [String: Int]) -> [String] {
    let productsSub = products.filter{$0.value < 1000}
    return Array(productsSub.keys)
}
print(productsBelowPrice(products: products))

// 2. Un closure que retorne el precio de los artículos en pesos colombianos (MapValues).
let exCol = 4400

let convertedPrices = products.mapValues { priceInDollars in
    return priceInDollars * exCol
}
print(convertedPrices)

// 3. Un closure que calcule el precio total de los artículos (Reduce).
let totalPrice = products.reduce(0) { (result, product) in
    return result + product.value
}

print("Precio total: $\(totalPrice)")


// Ejercicio 5

// 1. Construya una clase Gato que herede de Animal
// 2. Modifique el método description() para que represente mejor la información del gato. Pruebe e imprima el resultado en la consola.
// 3. Construya una propiedad computable para manejar un emoji asociado al gato. Si el nombre del gato es de menos de 5 letras, el emoji que se debe usar es: 🐱 , sino se debe usar: 🐈. Pruebe e imprima el resultado en la consola
// 4. Cree una propiedad que se llame alimento, que corresponde a una enumeración que puede tomar los valores Concentrado, Leche, Atún.
// 5. Modifique su clase para que cuando se haga un cambio sobre la propiedad edad, se modifique de la siguiente manera, si es menor a 10, el alimento debe ser concentrado, si esta entre 10 y 15 el alimento debe ser leche y finalmente si es mayor a 15 el alimento debe ser Atún. Pruebe e imprima el resultado en la consola.
// 6. Defina un protocolo nuevo que se llame AnimalNadador. Este protocolo agrega una propiedad distancia, que representa la cantidad de metros que puede nadar un animal y un método nadar.
// 7. Extienda su gato con el nuevo protocolo, el método nadar debe retornar una frase que indique la cantidad de metros que puede nadar un gato (p.ej. 30). Pruebe e imprima el resultado en la consola

class Animal {
    var age : Int
    var name : String
    let status = "Alive"

    init(age: Int, name: String){
        self.age = age
        self.name = name
    }

    func description() -> String{
        return "Animal \(name) is \(age) years old"
    }
}

// 6
protocol AnimalNadador {
    var distancia : Int {get}
    func nadar() -> String
}

// 4
enum Food : String {
    case Concentrado, Leche, Atun
}
var aFood = Food.Atun
print("Value: \(aFood)")
print("Raw Value: \(aFood.rawValue)")

// 1
class Cat : Animal{
    //5
    override var age : Int {
        willSet{
            if newValue < 10{
                self.food = Food.Concentrado
            } else if newValue >= 10 && newValue < 15 {
                self.food = Food.Leche
            } else {
                self.food = Food.Atun
            }
        }
    }
    // 4
    var food : Food = Food.Concentrado
    // 3
    var emoji : String {
        get{
            if(self.name.count<5){
                return "🐱"
            } else {
                return "🐈"
            }
        }
    }
    // 2
    override func description() -> String {
        return "Cat's name: \(name), age: \(age), it eats: \(food) "
    }
}
let 😸 = Cat(age: 5 , name: "Kittie")
print(😸.description())
😸.age = 10
print (😸.description())

extension Cat : AnimalNadador {
    var distancia : Int { return 30 }
    func nadar() -> String {
        return "El gato \(self.name) puede nadar \(distancia) metros."
        
    }
}
print (😸.nadar())
