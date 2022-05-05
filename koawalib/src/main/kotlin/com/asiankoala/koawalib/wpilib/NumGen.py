import re

s = '''
class N{} : Num() {
    override fun getNum(): Int {
        return {}
    }
}'''

for x in range(20):
    print(re.sub(r'\{\}', str(x), s))
    print()
