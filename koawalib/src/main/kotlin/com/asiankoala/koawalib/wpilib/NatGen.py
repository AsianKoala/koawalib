import re

s = '''
fun N{}(): Nat<Numbers.N{}> {
    return Nat { {} }
}
'''

for x in range(21):
    print(re.sub(r'\{\}', str(x), s))
    print()
