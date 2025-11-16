from rest_framework import serializers

from .models import Pet, Breed


# Исправить, костыльное повторение!!!!
class BreedShortSerializer(serializers.ModelSerializer):

    class Meta:
        model = Breed
        fields = ('id', 'name')


class PetShortSerializer(serializers.ModelSerializer):
    breed = BreedShortSerializer(read_only=True)

    class Meta:
        model = Pet
        fields = ('id', 'name', 'is_male', 'age', 'avatar', 'is_active',
                  'animal_type', 'breed')
