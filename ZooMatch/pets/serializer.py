from rest_framework import serializers

from .models import AnimalType, Breed, Tag, Pet, PetInfo, Comment
from users.serializers import UserSerializer


class AnimalTypeSerializer(serializers.ModelSerializer):

    class Meta:
        model = AnimalType
        fields = ('id', 'name')


class BreedSerializer(serializers.ModelSerializer):
    animal_type = AnimalTypeSerializer(read_only=True)

    class Meta:
        model = Breed
        fields = ('id', 'name', 'animal_type')


class TagSerializer(serializers.ModelSerializer):

    class Meta:
        model = Tag
        fields = ('id', 'tag')


class PetSerializer(serializers.ModelSerializer):
    owner = UserSerializer(read_only=True)
    animal_type = AnimalTypeSerializer(read_only=True)
    breed = BreedSerializer(read_only=True)
    tags = TagSerializer(many=True, read_only=True)

    class Meta:
        model = Pet
        fields = ('id', 'name', 'animal_type', 'breed', 'is_male',
                  'age', 'owner', 'avatar', 'location', 'has_pedigree',
                  'pedigree_documents', 'awards', 'tags', 'description',
                  'is_active')
        read_only_fields = ('id',)


class PetCreateUpdateSerializer(serializers.ModelSerializer):
    tags = serializers.ListField(
        child=serializers.CharField(max_length=50),
        required=False
    )

    class Meta:
        model = Pet
        fields = ('id', 'name', 'animal_type', 'breed', 'is_male',
                  'age', 'avatar', 'location', 'has_pedigree',
                  'pedigree_documents', 'awards', 'tags', 'description',
                  'is_active')
        read_only_fields = ('id',)

    def validate_tags(self, values):
        cleaned = []
        for value in values:
            tag_name = value.strip().lower()
            if tag_name not in cleaned:
                cleaned.append(tag_name)
        return cleaned

    def create(self, validated_data):
        tags_data = validated_data.pop('tags', [])
        pet = Pet.objects.create(**validated_data)

        for tag_name in tags_data:
            tag, _ = Tag.objects.get_or_create(tag=tag_name)
            pet.tags.add(tag)

        return pet
    
    def update(self, instance, validated_data):
        tags_data = validated_data.pop('tags', None)
        for attr, value in validated_data.items():
            setattr(instance, attr, value)
        instance.save()

        if tags_data is not None:
            instance.tags.clear()
            for tag_name in tags_data:
                tag, _ = Tag.objects.get_or_create(tag=tag_name)
                instance.tags.add(tag)
        
        return instance


class PetInfoSerializer(serializers.ModelSerializer):
    pet_id = serializers.PrimaryKeyRelatedField(
        queryset=Pet.objects.all(), source='pet'
    )

    class Meta:
        model = PetInfo
        fields = ('id', 'pet_id', 'watchers', 'likes')
        read_only_fields = ('id', 'pet_id', 'watchers', 'likes')


class CommentSerializer(serializers.ModelSerializer):
    author = UserSerializer()
    pet_info_card_id = serializers.PrimaryKeyRelatedField(
        queryset=PetInfo.objects.all(), source='pet_info_card'
    )

    class Meta:
        model = Comment
        fields = ('id', 'content', 'likes', 'author', 'pet_info_card_id',
                  'date_create')
        read_only_fields = ('id', 'likes', 'author', 'date_create')

    def create(self, validated_data):
        validated_data['author'] = self.context['request'].user
        return super().create(validated_data)
